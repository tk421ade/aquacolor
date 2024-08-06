package com.rubio.converter.backend.models;

public class Progress {
    private int min;
    private int max;
    private int progress;
    private String text = "0/0";

    public Progress(int min, int max, int progress) {
        this.min = min;
        this.max = max;
        this.progress = progress;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        this.text = progress + " / " + max;
    }

    public String getText() {
        return text;
    }
}
