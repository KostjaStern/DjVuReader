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
package com.sternkn.djvu.utils.utils;

import com.sternkn.djvu.file.coders.PixelColor;
import com.sternkn.djvu.file.coders.Pixmap;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ImageUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ImageUtils.class);

    private ImageUtils() {
    }

    public static Image toImage(Pixmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        LOG.debug("bitmap: border = {}, height = {}, width = {}", bitmap.getBorder(), height,  width);

        WritableImage image = new WritableImage(width, height);
        PixelWriter pixelWriter = image.getPixelWriter();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                PixelColor pixel = bitmap.getPixel(x, y);
                Color color = Color.rgb(pixel.getRed(), pixel.getGreen(), pixel.getBlue());
                pixelWriter.setColor(x, height - y - 1, color);
            }
        }

        return image;
    }

    public static Image composeImage(Pixmap m, Pixmap b, Pixmap f, int height, int width) {
        Image mask = toImage(m);
        Image background = toImage(b);

        if (background == null) {
            return mask;
        }
        if (mask == null) {
            return background;
        }

        Image foreground = toImage(f);

        double bgScale = background.getWidth() / width;
        double fgScale = foreground != null ? (foreground.getWidth() / width) : -1;
        LOG.debug("bgScale = {}", bgScale);
        LOG.debug("fgScale = {}", fgScale);

        WritableImage image = new WritableImage(width, height);
        PixelWriter writer = image.getPixelWriter();
        PixelReader maskReader = mask.getPixelReader();
        PixelReader fgReader = foreground == null ? null : foreground.getPixelReader();
        PixelReader bgReader = background.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = maskReader.getColor(x, y);

                Color resultColor = null;
                if (Color.WHITE.equals(color)) {
                    int srcX = Math.min((int) (x * bgScale), (int)(background.getWidth() - 1));
                    int srcY = Math.min((int) (y * bgScale), (int)(background.getHeight() - 1));
                    resultColor = bgReader.getColor(srcX, srcY);
                }
                else {
                    if (fgReader != null) {
                        int srcX = Math.min((int) (x * fgScale), (int)(foreground.getWidth() - 1));
                        int srcY = Math.min((int) (y * fgScale), (int)(foreground.getHeight() - 1));
                        resultColor = fgReader.getColor(srcX, srcY);
                    }
                    else {
                        resultColor = color;
                    }
                }

                writer.setColor(x, y, resultColor);
            }
        }

        return image;
    }
}
