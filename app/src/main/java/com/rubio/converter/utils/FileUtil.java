package com.rubio.converter.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
    public static String getFileNameWithoutExtension(String fileName) {
        // TODO name can be nasty, need to be cleaned up
        int index = fileName.lastIndexOf('.');
        if (index > 0) {
            return fileName.substring(0, index);
        } else {
            return fileName;
        }
    }
    public static String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index > 0) {
            return fileName.substring(index + 1);
        } else {
            return "";
        }
    }
    public static File writeFileOnInternalStorage(Context context, String folder, String filename, Bitmap bitmap){
        final String TAG = "writeFileOnInternalStorage";
        File dir = new File(context.getFilesDir(), folder);
        if(!dir.exists()){
            dir.mkdir();
        }

        String fullpath = dir.getPath() + "/" + filename;
        Log.i(TAG, "Writing file " + fullpath);
        try (FileOutputStream out = new FileOutputStream(fullpath)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
        } catch (IOException e) {
            Log.e(TAG, "Error while creating file", e);
        }
        return new File(fullpath);
    }
    public static void deleteRecursive(Context context, File fileOrDirectory) {
        fileOrDirectory = new File(context.getFilesDir() + "/" + fileOrDirectory);
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                FileUtil.deleteRecursive(context, child);
            }
        }
        Log.w("deleteRecursive", "Deleting " + fileOrDirectory.toPath().toString());
        fileOrDirectory.delete();
    }
}