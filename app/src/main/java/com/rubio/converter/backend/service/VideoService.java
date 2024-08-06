package com.rubio.converter.backend.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;
import com.rubio.converter.backend.models.Progress;
import com.rubio.converter.utils.FileUtil;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.IOException;

public class VideoService {
    final static String TAG = "EnhanceService/videoConversion";
    public static File clean(Context context, Uri inputUri, String videoFileName, Progress progress) {

        DebugService.send("[SaveFrame][" + videoFileName + "] Starting Video Conversion");
        MediaMetadataRetriever retriever =  new MediaMetadataRetriever();
        retriever.setDataSource(context, inputUri);

        String widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        int width = Integer.parseInt(widthStr);
        Log.v(VideoService.TAG, "width: " + width);
        int height = Integer.parseInt(heightStr);
        Log.v(TAG, "height: " + height);

        Integer fps = VideoService.getFPS(context, inputUri);
        Log.v(TAG, "fps: " + fps);

        if (progress.getProgress() == 0) {
            cleanTmpFiles(context, videoFileName);
        }
        File imagesFolder = convertToFrames(context, videoFileName, retriever, progress);
        String ffmpegFolderImages = imagesFolder.getAbsolutePath() + "/frame-%09d-converted.png";

        File videoPath = getVideoFile(videoFileName);

        String ffmpegCmd = "-y -framerate " + fps + " -pattern_type sequence -i '" + ffmpegFolderImages +  "' '" + videoPath + "'";
        Log.i(TAG, "Executing " + ffmpegCmd);

        FFmpegSession session = FFmpegKit.execute(ffmpegCmd);
        if (ReturnCode.isSuccess(session.getReturnCode())) {

            // SUCCESS
            Log.i(TAG, "Success");

        } else if (ReturnCode.isCancel(session.getReturnCode())) {

            // CANCEL
            Log.i(TAG, "Cancelled");

        } else {

            // FAILURE
            String error = String.format("Command failed with state %s and rc %s.%s", session.getState(), session.getReturnCode(), session.getFailStackTrace());
            DebugService.send("[SaveFrame][" + videoFileName + "] error: " + error);
            Log.d(TAG, error);

        }

        Log.v(TAG, "Cleaning temporal files");
        cleanTmpFiles(context, videoFileName);

        Log.v(TAG, "Video file fully converted.");
        DebugService.send("[SaveFrame][" + videoFileName + "] Completed");

        return videoPath;
    }

    private static File getVideoFile(String videoFileName) {
        String extension = "mp4";
        File picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String nameNoExtension = FileUtil.getFileNameWithoutExtension(videoFileName);
        int count = 1;
        File videoWriterFullPath = new File(picturesDirectory + "/" + nameNoExtension + "-CLEANED." + extension);
        while ( videoWriterFullPath.exists() ) {
            videoWriterFullPath = new File(picturesDirectory + "/" + nameNoExtension + "-" +  count + "-CLEANED." + extension);
            count +=1;
        }
        Log.i(TAG, "(VW) VideoPath:   " + videoWriterFullPath.getPath());
        return videoWriterFullPath;
    }

    private static void cleanTmpFiles(Context context, String videoFileName) {
        String tmpFolderName = FileUtil.getFileNameWithoutExtension(videoFileName);
        Log.i(TAG, "Cleaning up internal storage folder " + tmpFolderName);
        FileUtil.deleteRecursive(context, new File(tmpFolderName));
    }

    private static File convertToFrames(Context context, String videoFileName, MediaMetadataRetriever retriever, Progress progress) {

        int frameCount = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT));
        Log.v(TAG, "frameCount: " + frameCount);
        progress.setMax(frameCount);

        String tmpFolderName = FileUtil.getFileNameWithoutExtension(videoFileName);

        File imagesFolder = null;
        int startFrame = progress.getProgress();
        for (int frameIndex = startFrame; frameIndex < frameCount; frameIndex += 1) { // increment by 1 second (1000000 microseconds)
            Bitmap frame = retriever.getFrameAtIndex(frameIndex);
            if (frame == null) {
                Log.w(TAG, "Frame is null at frameIndex: " + frameIndex);
            } else {
                String formattedFrameIndex = String.format("%09d", frameIndex);
                String tempFilename = "frame-" + formattedFrameIndex + "-converted.png";
                //String tempOriFilename = "frame-" + frameIndex + "-original.png";
                //FileUtil.writeFileOnInternalStorage(context, folder, tempOriFilename, frame);
                Log.i(TAG, "Processing frameIndex: " + frameIndex);
                Bitmap convertedBitmap = ImageService.enhanceFrame(frame);
                File savedFile = FileUtil.writeFileOnInternalStorage(context, tmpFolderName, tempFilename, convertedBitmap);
                if ( imagesFolder == null ) {
                    imagesFolder = savedFile.getParentFile();
                }
                Log.i(TAG, "Completed");
            }
            progress.setProgress(frameIndex);
        }
        return imagesFolder;
    }

    private static Integer getFPS(Context context, Uri uri) {

        if (OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "OpenCV loaded Successfully!");
        } else {
            Log.e("OpenCV", "Unable to load OpenCV!");
        }

        final String TAG = "EnhanceService/getFPS";
        Integer fps = null;
        MediaExtractor mediaExtractor = new MediaExtractor();
        try {
            mediaExtractor.setDataSource(context, uri, null);
        } catch (IOException e) {
            Log.e(TAG, "Enable to open inputUri");
        }
        int numTracks = mediaExtractor.getTrackCount();
        for (int i = 0; i < numTracks; ++i) {
            MediaFormat format = mediaExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                    fps = format.getInteger(MediaFormat.KEY_FRAME_RATE);
                }
            }
        }
        mediaExtractor.release();
        return fps;

    }
}
