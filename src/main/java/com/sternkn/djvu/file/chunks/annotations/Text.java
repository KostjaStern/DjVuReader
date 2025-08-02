package com.sternkn.djvu.file.chunks.annotations;

import com.sternkn.djvu.file.chunks.Color;

public class Text extends Area {
    private final int xmin;
    private final int ymin;
    private final int width;
    private final int height;

    private Color backgroundColor;
    private Color textColor;
    private boolean pushPin;

    public Text(int xmin, int ymin, int width, int height) {
        super(AreaType.TEXT_BOX);
        this.xmin = xmin;
        this.ymin = ymin;
        this.width = width;
        this.height = height;
    }

    public Text setBorder(Border border) {
        this.border = border;
        return this;
    }

    public Text setBackgroundColor(Color color) {
        this.backgroundColor = color;
        return this;
    }
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public Text setTextColor(Color color) {
        this.textColor = color;
        return this;
    }
    public Color getTextColor() {
        return textColor;
    }

    public Text setPushPin(boolean pushPin) {
        this.pushPin = pushPin;
        return this;
    }
    public boolean isPushPin() {
        return pushPin;
    }

    public int getXmin() {
        return xmin;
    }

    public int getYmin() {
        return ymin;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
