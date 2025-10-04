package com.sternkn.djvu.file.coders;

import com.sternkn.djvu.file.DjVuFileException;

import java.util.Arrays;
import java.util.Objects;

import static com.sternkn.djvu.file.utils.NumberUtils.asUnsignedShort;

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
