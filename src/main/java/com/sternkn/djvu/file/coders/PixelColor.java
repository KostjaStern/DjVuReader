package com.sternkn.djvu.file.coders;

public class PixelColor {

    private int blue;
    private int green;
    private int red;

    public PixelColor() {
    }

    public void setColor(ColorName colorName, int value) {
        switch (colorName) {
            case BLUE:
                blue = value;
                break;
            case GREEN:
                green = value;
                break;
            case RED:
                red = value;
        }
    }

    public int getBlue() {
        return blue;
    }
    public int getGreen() {
        return green;
    }
    public int getRed() {
        return red;
    }
}
