package com.rubio.converter

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.rubio.converter.backend.service.DebugService
import com.rubio.converter.backend.service.ImageService
import com.rubio.converter.backend.service.UIService
import com.rubio.converter.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader
import java.io.File


class MainActivity : AppCompatActivity() {
    companion object {
        val PERMISSIONS = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        const val REQUEST_PERMISSIONS = 1
        const val INTENT_OPEN_VIDEO = 2
        //const val REQUEST_CODE_OUTPUT_VIDEO_FILE = 3
    }

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var videoUri: Uri? = null
    private var videoName  = ""
    private var videoDuration = 0
    private var videoPosition = 0
    private var saveFrameMenuItem: MenuItem? = null
    private var mediaPlayer: MediaPlayer? = null
    private var mediaMetadataRetriever = MediaMetadataRetriever()
    var processFrameThread: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!askPermissions())
            onPermissionsAllowed()

        if (OpenCVLoader.initLocal()) {
            Log.i("OpenCV", "OpenCV successfully loaded.");
        } else {
            Log.e("OpenCV", "OpenCV NOT  loaded.");
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSIONS -> handleRequestPermissions(grantResults)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun exitApp() {
        setResult(0)
        finish()
    }

    private fun fatalError(msg: String) {
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.app_name))
                .setMessage(msg)
                .setIcon(android.R.drawable.stat_notify_error)
                .setCancelable(false)
                .setPositiveButton("OK") { _, _ -> exitApp() }
                .show()
    }

    private fun askPermissions(): Boolean {
        for (permission in PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSIONS)
                return true
            }
        }

        return false
    }

    private fun handleRequestPermissions(grantResults: IntArray) {
        var allowedAll = grantResults.size >= PERMISSIONS.size

        if (grantResults.size >= PERMISSIONS.size) {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allowedAll = false
                    break
                }
            }
        }

        if (allowedAll) onPermissionsAllowed()
        else fatalError("You must allow permissions !")
    }

    private fun onPermissionsAllowed() {
        BusyDialog.create(this)

        binding.seekBarPosition.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, p2: Boolean) {
                setVideoPosition(progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })

        binding.buttonSubMax.setOnClickListener { shiftVideoPosition(-2000) }
        binding.buttonSubMed.setOnClickListener { shiftVideoPosition(-500) }
        binding.buttonSubMin.setOnClickListener { shiftVideoPosition(-33) }
        binding.buttonAddMin.setOnClickListener { shiftVideoPosition(33) }
        binding.buttonAddMed.setOnClickListener { shiftVideoPosition(500) }
        binding.buttonAddMax.setOnClickListener { shiftVideoPosition(2000) }

        binding.videoView.setAudioFocusRequest(AudioManager.AUDIOFOCUS_NONE)
        binding.videoView.setOnPreparedListener { newMediaPlayer ->
            newMediaPlayer.setVolume(0.0f, 0.0f)
            videoDuration = newMediaPlayer.duration
            mediaPlayer = newMediaPlayer
            updateAll()
            UIService.showVideoControls(binding);
            Log.i("[Video]", "duration: $videoDuration")
        }

        binding.imageProgressBar.visibility = View.GONE
        binding.menuSaveVideo.visibility = View.INVISIBLE
        binding.menuSaveFrame.visibility = View.INVISIBLE
        binding.menuSaveFrame.setOnClickListener { handleSaveFrame() }
        binding.oneTimeOpenVideo.setOnClickListener { handleOpenVideo() }
        binding.menuSaveVideo.setOnClickListener { performVideoConversion() }

        UIService.hideVideoControls(binding);

        setContentView(binding.root)
    }

    private fun performVideoConversion() {

        val startConverionIntent = Intent(this@MainActivity, ConversionActivity::class.java)
        startConverionIntent.putExtra("videoUri", videoUri.toString()) //Optional parameters
        this@MainActivity.startActivity(startConverionIntent)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_menu, menu)
        //saveFrameMenuItem = menu?.findItem(R.id.menuSaveFrame)
        //saveFrameMenuItem?.isEnabled = null != videoUri && videoDuration > 0
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menuOpenVideo -> handleOpenVideo()
            //R.id.menuSaveFrame -> handleSaveFrame()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, intent)
        DebugService.send("Opening Video result (RESULT_OK is -1):  $resultCode")
        if (requestCode == INTENT_OPEN_VIDEO) {
            if (resultCode == RESULT_OK) {
                intent?.data?.let { uri -> openVideo(uri) }
                return

            }
        }
    }

    private fun saveFrame() {
        var success = false
        val fileName = "frame_${System.currentTimeMillis()}.png"
        try {
            mediaMetadataRetriever.setDataSource(applicationContext, videoUri)
            mediaMetadataRetriever.getFrameAtTime(videoPosition * 1000L, MediaMetadataRetriever.OPTION_CLOSEST)?.let{ frameBitmap ->
                @Suppress("DEPRECATION")
                val picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val fileFullPath = "$picturesDirectory/$fileName"

                val outputStream = File(fileFullPath).outputStream()
                frameBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()

                val values = ContentValues()
                @Suppress("DEPRECATION")
                values.put(MediaStore.Images.Media.DATA, fileFullPath)
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                val newUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                success = newUri != null
                DebugService.send("[SaveFrame] Success:  $success")
            }
        } catch (e: Exception) {
            Log.e("SaveFrame", "Failed Saving Frame", e)
            DebugService.send("[SaveFrame] error:  ${e.stackTraceToString()}")
        }

        runOnUiThread {
            BusyDialog.dismiss()
            Toast.makeText(applicationContext, if (success) "Saved: $fileName" else "Failed !", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleSaveFrame() {
        BusyDialog.show(supportFragmentManager)
        GlobalScope.launch(Dispatchers.IO) { saveFrame() }
    }


    private fun handleOpenVideo() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .putExtra("android.content.extra.SHOW_ADVANCED", true)
            .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
            .putExtra(Intent.EXTRA_TITLE, "Select video")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("video/*")
        UIService.showVideoControls(binding);
        @Suppress("DEPRECATION")
        startActivityForResult(intent, INTENT_OPEN_VIDEO)
    }

    private fun openVideo(videoUri: Uri) {
        mediaPlayer = null
        this.videoUri = videoUri
        binding.videoView.setVideoURI(videoUri)
        videoDuration = 0
        videoName = DocumentFile.fromSingleUri(applicationContext, videoUri)?.name ?: ""
        updateAll()
        UIService.showVideoControls(binding);
    }

    private fun updateAll() {
        val enabled = videoDuration > 0
        binding.seekBarPosition.isEnabled = enabled
        binding.seekBarPosition.progress = 0
        binding.buttonAddMax.isEnabled = enabled
        binding.buttonAddMed.isEnabled = enabled
        binding.buttonAddMin.isEnabled = enabled
        binding.buttonSubMin.isEnabled = enabled
        binding.buttonSubMed.isEnabled = enabled
        binding.buttonSubMax.isEnabled = enabled
        binding.txtVideoName.text = if (enabled) videoName else ""
        if (enabled) {
            binding.seekBarPosition.max = videoDuration
            setVideoPosition(0, true)
        }
    }

    private fun shiftVideoPosition(delta: Int) {
        setVideoPosition(videoPosition + delta)
    }

    private fun setVideoPosition(newVideoPosition: Int, force: Boolean = false) {
        if (!force && newVideoPosition == this.videoPosition) return
        if (newVideoPosition < 0 || newVideoPosition >= videoDuration || videoDuration <= 0) return

        this.videoPosition = newVideoPosition
        binding.seekBarPosition.progress = newVideoPosition
        mediaPlayer?.seekTo(videoPosition.toLong(), MediaPlayer.SEEK_CLOSEST)

        processFrameThread?.let {
                if (it.isAlive) {
                    // Cancel the running thread
                    it.interrupt()
                    UIService.frameConverted(binding);
                }
            }
        binding.imageProgressBar.visibility = View.VISIBLE
        UIService.convertingFrame(binding);
        processFrameThread = Thread {
                try {
                    mediaMetadataRetriever.setDataSource(applicationContext, videoUri)
                    mediaMetadataRetriever.getFrameAtTime(videoPosition * 1000L, MediaMetadataRetriever.OPTION_CLOSEST)
                        ?.let { frame ->
                            val sourceBitmap = ImageService.enhanceFrame(frame)
                            //val sourceBitmap = frame.copy(Bitmap.Config.ARGB_8888, true);
                            //Utils.matToBitmap(sourceMat, sourceBitmap);
                            val imageView = findViewById<View>(R.id.imageView) as ImageView
                            runOnUiThread(Runnable() {
                                imageView.setImageBitmap(sourceBitmap);
                                UIService.frameConverted(binding);
                            });
                        }
                } catch (e: Exception) {
                    Log.e("setVideoPosition", "Failed Loading Frame", e)
                }
            }.apply {
                start()
            }

    }
}