/*
    Copyright (C) 2025 Kostya Stern

    This program is free software; you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation; either version 2 of the License, or (at your option)
    any later version.

    This program is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
    more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc., 51
    Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
*/
package com.sternkn.djvu.file.coders;

import com.sternkn.djvu.file.DjVuFileException;

import java.util.Arrays;
import java.util.Objects;

import static com.sternkn.djvu.utils.NumberUtils.asUnsignedShort;

public class GPixmap implements Pixmap {

    private final int height;
    private final int width;
    private final PixelColor[] pixels;

    public GPixmap(int h, int w) {
        height = h;
        width = w;
        int np = height * width;

        if (height != asUnsignedShort(height) ||
            width != asUnsignedShort(width) ||
            (height > 0 && np/height != width) ) {
            throw new DjVuFileException("GPixmap: image size exceeds maximum (corrupted file?)");
        }

        this.pixels = new PixelColor[np];
    }

    public PixelColor[] getPixels() {
        return pixels;
    }

    /*
         x - 0 ... width - 1
         y - 0 ... height - 1
    */
    @Override
    public PixelColor getPixel(int x, int y) {
        return pixels[y * width + x];
    }

    public void setPixel(int x, int y, PixelColor pixel) {
        pixels[y * width + x] = pixel;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getBorder() {
        return 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(height, width, Arrays.hashCode(pixels));
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof GPixmap pixmap)) {
            return false;
        }

        return pixmap.getHeight() == height &&
                pixmap.getWidth() == width &&
                Arrays.equals(pixmap.getPixels(), pixels);
    }

    @Override
    public String toString() {
        return String.format("GPixmap{height: %s, width: %s}", height, width);
    }
}
