package com.rubio.converter.backend.service;

import android.view.View;
import com.rubio.converter.databinding.ActivityMainBinding;

public class UIService {

    public static void videoNotLoaded(ActivityMainBinding binding) {

    }
    public static void showVideoControls(ActivityMainBinding binding) {

        int visibility = View.VISIBLE;
        binding.oneTimeOpenVideo.setVisibility(View.GONE);
        displayVideoControls(binding, visibility);
    }

    public static void hideVideoControls(ActivityMainBinding binding) {

        int visibility = View.GONE;
        //int visibility = View.VISIBLE;
        binding.oneTimeOpenVideo.setVisibility(View.VISIBLE);
        displayVideoControls(binding, visibility);
    }

    private static void displayVideoControls(ActivityMainBinding binding, int visibility) {
        binding.buttonAddMax.setVisibility(visibility);
        binding.buttonAddMed.setVisibility(visibility);
        binding.buttonAddMin.setVisibility(visibility);
        binding.buttonSubMin.setVisibility(visibility);
        binding.buttonSubMed.setVisibility(visibility);
        binding.buttonSubMax.setVisibility(visibility);
        binding.seekBarPosition.setVisibility(visibility);
        binding.videoView.setVisibility(visibility);
    }

    public static void convertingFrame(ActivityMainBinding binding) {
        binding.imageProgressBar.setVisibility(View.VISIBLE);
        binding.imageView.setVisibility(View.INVISIBLE);
        //binding.imageView.setVisibility(View.VISIBLE);
        binding.menuSaveVideo.setVisibility(View.INVISIBLE);
        binding.menuSaveFrame.setVisibility(View.INVISIBLE);
    }
    public static void frameConverted(ActivityMainBinding binding) {
        binding.imageProgressBar.setVisibility(View.GONE);
        binding.imageView.setVisibility(View.VISIBLE);
        binding.menuSaveVideo.setVisibility(View.VISIBLE);
        binding.menuSaveFrame.setVisibility(View.VISIBLE);
    }

    public static void hideFrameConversion(ActivityMainBinding binding) {
        binding.imageProgressBar.setVisibility(View.GONE);
        binding.imageView.setVisibility(View.GONE);
        binding.menuSaveVideo.setVisibility(View.GONE);
        binding.menuSaveFrame.setVisibility(View.GONE);
    }

    public static void hideOpenVideo(ActivityMainBinding binding) {
        binding.oneTimeOpenVideo.setVisibility(View.GONE);
    }

}
