package com.sternkn.djvu.file.coders;

public interface Pixmap {

    int getHeight();

    int getWidth();

    int getBorder();

    PixelColor getPixel(int x, int y);
}
