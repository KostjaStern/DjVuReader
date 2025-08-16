package com.sternkn.djvu.file.chunks.annotations;

import com.sternkn.djvu.file.chunks.Color;

import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Text other)) {
            return false;
        }

        return type == other.getType()
                && Objects.equals(this.border, other.border)
                && this.xmin == other.xmin
                && this.ymin == other.ymin
                && this.width == other.width
                && this.height == other.height
                && Objects.equals(this.backgroundColor, other.backgroundColor)
                && Objects.equals(this.textColor, other.textColor)
                && this.pushPin == other.pushPin;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, border, xmin, ymin, width, height, backgroundColor, textColor, pushPin);
    }
}
