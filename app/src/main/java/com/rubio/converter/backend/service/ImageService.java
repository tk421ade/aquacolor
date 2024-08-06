package com.rubio.converter.backend.service;

import android.graphics.Bitmap;
import com.rubio.converter.backend.algoritms.FusionEnhance;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class ImageService {



    public static Bitmap enhanceFrame(Bitmap frame) {
        Mat resultMat;
        Mat sourceMat = new Mat(frame.getHeight(), frame.getWidth(), CvType.CV_8UC3);
        Bitmap sourceBitmap = frame.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(sourceBitmap, sourceMat);

        // remove blue background
        resultMat = FusionEnhance.enhance(sourceMat, 5);
        resultMat.convertTo(resultMat, CvType.CV_8UC1);

        // Adjust brightness automagically
        List<Mat> channels = new ArrayList<>();
        Core.split(resultMat, channels);
        for (Mat channel : channels) {
            Imgproc.equalizeHist(channel, channel);
        }
        Core.merge(channels, resultMat);

        Utils.matToBitmap(resultMat, sourceBitmap);
        return sourceBitmap;
        //return resultMat;
    }















    public static void nobs () {
        // TODO


        //                    val sourceMat = Mat(frame.getHeight(), frame.getWidth(), CvType.CV_8UC4)
//                    var resultMat = Mat()
//                    val sourceBitmap: Bitmap = frame.copy(Bitmap.Config.ARGB_8888, true)
//                    Utils.bitmapToMat(sourceBitmap, sourceMat)

        //black and white - works
        //Imgproc.cvtColor(sourceMat,resultMat,Imgproc.COLOR_RGBA2GRAY);

        // sepia transform - does not work
//                    var sepiaKernel =  Mat(3, 3, CvType.CV_32F);
//                    sepiaKernel.put(0, 0, 0.272,0.534 , 0.131)
//                    sepiaKernel.put(1, 0, 0.349, 0.686,  0.168)
//                    sepiaKernel.put(2, 0, 0.393, 0.769, 0.189)
//                    Core.transform(sourceMat, resultMat, sepiaKernel)


        // RGB TEST - works
//                    val redValue = 1.0
//                    val greenValue = 1.0
//                    val blueValue = 0.8
//                    val channelsRGB = ArrayList<Mat>()
//                    Core.split(sourceMat, channelsRGB)
//                    Core.multiply(channelsRGB[0], Scalar(redValue), channelsRGB[0])
//                    Core.multiply(channelsRGB[1], Scalar(greenValue), channelsRGB[1])
//                    Core.multiply(channelsRGB[2], Scalar(blueValue), channelsRGB[2])
//                    Core.merge(channelsRGB,resultMat)

        // HSV TEST
//                    val HValue = 1.0
//                    val SValue = 1.0
//                    val VValue = 1.0
//                    val channelsHSV = ArrayList<Mat>()
//                    Imgproc.cvtColor(sourceMat, resultMat, Imgproc.COLOR_BGR2HSV)
//                    Core.split(resultMat, channelsHSV)
//                    Core.multiply(channelsHSV[0], Scalar(HValue), channelsHSV[0])
//                    Core.multiply(channelsHSV[1], Scalar(SValue), channelsHSV[1])
//                    Core.multiply(channelsHSV[2], Scalar(VValue), channelsHSV[2])
//                    Core.merge(channelsHSV, resultMat)
//                    Imgproc.cvtColor(resultMat, resultMat, Imgproc.COLOR_HSV2BGR)
    }
}
