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

import com.sternkn.djvu.file.chunks.ImageRotationType;
import com.sternkn.djvu.file.coders.IW44Image;
import com.sternkn.djvu.file.coders.JB2CodecDecoder;
import com.sternkn.djvu.file.coders.JB2Dict;
import com.sternkn.djvu.file.coders.JB2Image;
import com.sternkn.djvu.file.coders.PixelColor;
import com.sternkn.djvu.file.coders.Pixmap;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

public final class ImageUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ImageUtils.class);

    private ImageUtils() {
    }

    public static Image toImage(Pixmap bitmap, ImageRotationType rotationType) {
        if (bitmap == null) {
            return null;
        }

        int height = rotatedHeight(bitmap.getWidth(), bitmap.getHeight(), rotationType);
        int width = rotatedWidth(bitmap.getWidth(), bitmap.getHeight(), rotationType);
        LOG.debug("bitmap: border = {}, height = {}, width = {}", bitmap.getBorder(), height,  width);

        WritableImage image = new WritableImage(width, height);
        PixelWriter pixelWriter = image.getPixelWriter();

        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                PixelColor pixel = bitmap.getPixel(x, y);
                Color color = Color.rgb(pixel.getRed(), pixel.getGreen(), pixel.getBlue());

                int newY = rotatedY(x, y, rotationType, height);
                int newX = rotatedX(x, y, rotationType, width);
                pixelWriter.setColor(newX, newY, color);
            }
        }

        return image;
    }

    private static int rotatedHeight(int width, int height, ImageRotationType rotationType) {
        if (rotationType == ImageRotationType.NO_ROTATION || rotationType == ImageRotationType.UPSIDE_DOWN) {
            return height;
        }

        return width;
    }

    private static int rotatedWidth(int width, int height, ImageRotationType rotationType) {
        if (rotationType == ImageRotationType.NO_ROTATION || rotationType == ImageRotationType.UPSIDE_DOWN) {
            return width;
        }

        return height;
    }

    private static int rotatedX(int x, int y, ImageRotationType rotationType, int width) {
        return switch (rotationType) {
            case CLOCKWISE_90 -> width - y - 1;
            case COUNTER_CLOCKWISE_90 -> y;
            default -> x;
        };
    }

    private static int rotatedY(int x, int y, ImageRotationType rotationType, int height) {
        return switch (rotationType) {
            case CLOCKWISE_90 -> x;
            case UPSIDE_DOWN -> height - y - 1;
            case COUNTER_CLOCKWISE_90 -> height - x - 1;
            default -> y;
        };
    }

    public static Image composeImage(Pixmap m, Pixmap b, Pixmap f, int height, int width) {
        return composeImage(m, b, f, height, width, ImageRotationType.NO_ROTATION);
    }

    public static Image composeImage(Pixmap m, Pixmap b, Pixmap f, int height, int width, ImageRotationType rotationType) {
        Image mask = toImage(m, rotationType);
        Image background = toImage(b, rotationType);

        if (background == null) {
            return mask;
        }
        if (mask == null) {
            return background;
        }

        Image foreground = toImage(f, rotationType);

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

    public static JB2Image decodeBitonalImage(byte[] data, byte[] dict) {
        JB2Dict dictionary = null;
        if (dict != null) {
            dictionary = new JB2Dict();
            JB2CodecDecoder decoder = new JB2CodecDecoder(new ByteArrayInputStream(dict));
            decoder.decode(dictionary);
        }

        JB2Image image = new JB2Image(dictionary);
        JB2CodecDecoder decoder = new JB2CodecDecoder(new ByteArrayInputStream(data));
        decoder.decode(image);

        return image;
    }

    public static IW44Image decodeColorImage(List<byte[]> data) {
        final IW44Image image = new IW44Image();
        data.forEach(image::decode_chunk);
        image.close_codec();

        return image;
    }

    public static void saveAsFile(Image image, String filename) {

        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelReader reader = image.getPixelReader();

        BufferedImage buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = reader.getArgb(x, y);

                buffer.setRGB(x, y, color);
            }
        }

        File outputfile = new File(filename);

        try {
            ImageIO.write(buffer, "png", outputfile);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
