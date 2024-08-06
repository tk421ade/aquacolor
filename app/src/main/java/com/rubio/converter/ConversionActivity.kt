package com.rubio.converter

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.rubio.converter.backend.models.Inspiration
import com.rubio.converter.backend.models.Progress
import com.rubio.converter.backend.service.DebugService.send
import com.rubio.converter.backend.service.VideoService
import com.rubio.converter.databinding.ActivityConversionBinding
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ConversionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConversionBinding
    private val TAG = "ConversionActivity"
    private var STATUS = "NOT_STARTED"
    private var VIDEO_URI :String = ""
    private var progress = Progress(0, 100, 0);
    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_conversion)
        super.onCreate(savedInstanceState)
        Log.i(TAG, "OK, On Create")
        binding = ActivityConversionBinding.inflate(layoutInflater)
        val view = binding.root
        binding.startAgain.setOnClickListener({
            Log.i(TAG, "Finishing Activity")
            finish()
        })
        setContentView(view)
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "OK, On Resume")
        videoConversion()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Log.i(TAG, "OK, onRestoreInstanceState")
        Log.i(TAG, "Status: $STATUS")
        // textView.text = savedInstanceState?.getString(TEXT_VIEW_KEY)
        if (STATUS == "NOT_STARTED" || STATUS == "PAUSED") {
            STATUS = savedInstanceState.getString("STATUS").toString()
            Log.i(TAG, "Restored Status: $STATUS")
            VIDEO_URI = savedInstanceState.getString("VIDEO_URI").toString()
            progress.max = Integer.parseInt(savedInstanceState.getString("PROGRESS_MAX").toString())
            progress.progress = Integer.parseInt(savedInstanceState.getString("PROGRESS_PROGRESS").toString())
            Log.i(TAG, "Restored progress. max: ${progress.max} progress: ${progress.progress}")
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        Log.i(TAG, "OK, onSaveInstanceState")
        Log.i(TAG, "Status: $STATUS")
        if ( STATUS == "IN_PROGRESS" ) {
            Log.i(TAG, "Changing state from: $STATUS to PAUSED")
            STATUS = "PAUSED"
        }
        outState.run {
            putString("STATUS", STATUS)
            putString("VIDEO_URI", VIDEO_URI)
            putString("PROGRESS_MAX", progress.max.toString())
            putString("PROGRESS_PROGRESS", progress.progress.toString())
        }
        super.onSaveInstanceState(outState)

    }


    override fun onPause() {
        Log.i(TAG, "OK, On Pause")
        super.onPause()
    }

    private fun videoConversion() {

        val videoUri:Uri = Uri.parse(intent.getStringExtra("videoUri"))

        val videoName = videoUri?.let {
            DocumentFile.fromSingleUri(applicationContext, it)?.name } ?: ""

        val progressScheduler = Executors.newSingleThreadScheduledExecutor()
        val futureProgressScheduler = progressScheduler.scheduleAtFixedRate(Runnable {
            Log.v("progressScheduler", "updating progress: ${progress.text}")
            runOnUiThread {
                binding.txtProgress.text = "Converting Video: ${progress.text}"
                binding.conversionProgressBar.max = progress.max
                binding.conversionProgressBar.min = progress.min
                binding.conversionProgressBar.progress = progress.progress
            }
        }, 0, 1, TimeUnit.SECONDS)

        val inspirationsScheduler = Executors.newSingleThreadScheduledExecutor()
        val futureInspirations = inspirationsScheduler.scheduleAtFixedRate(Runnable {
            Log.v("inspirationsScheduler", "Loading new inspiration ...")
            try {
                val inspiration = Inspiration.getRandomPhrase()
                Log.v("inspirationsScheduler", "New inspiration: $inspiration")
                runOnUiThread {
                    binding.txtInspirational.text = inspiration
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting a new inspiration ...")
            }

        }, 0, 10, TimeUnit.SECONDS)

        Thread {
            try {
                Log.i(TAG, "Status: $STATUS")
                if (STATUS == "NOT_STARTED" || STATUS == "PAUSED") {
                    Log.i(TAG, "Changing state from: $STATUS to IN_PROGRESS")
                    STATUS = "IN_PROGRESS"
                    val videoPath = VideoService.clean(applicationContext, videoUri, videoName, progress)
                    runOnUiThread {
                        binding.conversionProgressBar.progress = binding.conversionProgressBar.max
                        futureProgressScheduler.cancel(true)
                        futureInspirations.cancel(true)
                        Toast.makeText(applicationContext, "Saved: Downloads > ${videoPath.name}", Toast.LENGTH_LONG)
                            .show()
                        val finalMessage = "Converted video saved at Downloads"
                        binding.txtProgress.text = finalMessage
                        binding.txtInspirational.text = "Completed. Do you want to convert another video?"
                        binding.startAgain.visibility = View.VISIBLE
                        STATUS = "COMPLETED"
                    }
                }
            } catch (e: Exception) {
                Log.e("videoConversion", "Failed Converting Video", e)
                send("[SaveFrame][$videoName] Failed Converting Video: ${e.stackTraceToString()}")
            }
        }.apply {
            start()
        }
    }
}