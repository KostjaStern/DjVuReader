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
package com.sternkn.djvu.utils;

import com.sternkn.djvu.file.coders.PixelColor;
import com.sternkn.djvu.file.coders.Pixmap;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class PNGPixmap implements Pixmap {

    private final int height;
    private final int width;
    private final PixelColor[] pixels;

    public PNGPixmap(File file) {
        final BufferedImage img = readImage(file);

        this.height = img.getHeight();
        this.width = img.getWidth();
        this.pixels = new PixelColor[this.width * this.height];

        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                int argb = img.getRGB(x, y);
                final int red   = (argb >> 16) & 0xFF;
                final int green = (argb >> 8)  & 0xFF;
                final int blue  = argb         & 0xFF;
                PixelColor color = new PixelColor(blue, green, red);
                setPixel(x, y, color);
            }
        }
    }

    public PNGPixmap(Image image) {

        this.height = (int) image.getHeight();
        this.width = (int) image.getWidth();
        this.pixels = new PixelColor[this.width * this.height];

        PixelReader reader = image.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);
                final int red   = (int) (color.getRed() * 255);
                final int green = (int) (color.getGreen() * 255);
                final int blue  = (int) (color.getBlue() * 255);
                setPixel(x, y, new PixelColor(blue, green, red));
            }
        }
    }

    private BufferedImage readImage(File file) {
        try {
            BufferedImage src = ImageIO.read(file);
            BufferedImage img = src.getType() == BufferedImage.TYPE_INT_RGB ? src :
                    new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
            img.getGraphics().drawImage(src, 0, 0, null);
            return img;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    public PixelColor getPixel(int x, int y) {
        return pixels[y * width + x];
    }

    public void setPixel(int x, int y, PixelColor pixel) {
        pixels[y * width + x] = pixel;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PNGPixmap other)) {
            return false;
        }

        return width == other.width
            && height == other.height
            && Arrays.equals(pixels, other.pixels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, Arrays.hashCode(pixels));
    }
}
