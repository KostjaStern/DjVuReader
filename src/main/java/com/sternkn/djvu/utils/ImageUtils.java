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
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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

    /**
     * Converts a bitmap to an image, applying the specified rotation.
     *
     * @param bitmap the source bitmap
     * @param rotationType the rotation to apply
     * @return the rotated image
     */
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

    /**
     * Returns an image at the maximum size that fits within a {@code targetWidth} × {@code targetHeight} rectangle.
     * This method can be called only from the FX application thread.
     *
     * @param src          the source image
     * @param targetWidth  the target rectangle width (in pixels)
     * @param targetHeight the target rectangle height (in pixels)
     * @return the resized image
     */
    public static Image resizeImage(Image src, int targetWidth, int targetHeight) {
        if (src == null) {
            throw new IllegalArgumentException("src is null");
        }
        if (targetWidth <= 0 || targetHeight <= 0) {
            throw new IllegalArgumentException("Invalid size");
        }

        final double srcWidth = src.getWidth();
        final double srcHeight = src.getHeight();
        final double scale = Math.min(targetWidth / srcWidth, targetHeight / srcHeight);
        int width = (int) Math.round(srcWidth * scale);
        int height = (int) Math.round(srcHeight * scale);

        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setImageSmoothing(true);
        gc.drawImage(src, 0, 0, width, height);

        WritableImage image = new WritableImage(width, height);
        canvas.snapshot(null, image);
        return image;
    }

    /**
     * Returns an image at the maximum size that fits within a {@code targetWidth} × {@code targetHeight} rectangle.
     * Resizes using nearest-neighbor interpolation. This method may be called from a non-FX application thread.
     *
     * @param src          the source image
     * @param targetWidth  the target rectangle width (in pixels)
     * @param targetHeight the target rectangle height (in pixels)
     * @return the resized image
     */
    public static Image resize(Image src, int targetWidth, int targetHeight) {
        if (src == null) {
            throw new IllegalArgumentException("src is null");
        }
        if (targetWidth <= 0 || targetHeight <= 0) {
            throw new IllegalArgumentException("Invalid size");
        }

        PixelReader reader = src.getPixelReader();

        WritableImage image = new WritableImage(targetWidth, targetHeight);
        PixelWriter writer = image.getPixelWriter();

        final double srcWidth = src.getWidth();
        final double srcHeight = src.getHeight();
        final double scale = Math.min(srcWidth / targetWidth, srcHeight / targetHeight);

        for (int y = 0; y < targetHeight; y++) {
            int srcY = Math.min((int) (y * scale), (int) srcHeight - 1);

            for (int x = 0; x < targetWidth; x++) {
                int srcX = Math.min((int) (x * scale), (int) srcWidth - 1);
                writer.setArgb(x, y, reader.getArgb(srcX, srcY));
            }
        }
        return image;
    }

    /**
     * Returns a composite image rendered by painting the foreground color image over the background color image,
     * using the foreground mask as a stencil.
     *
     * @param mask the foreground bitonal mask; may be {@code null}
     * @param background the background color image; may be {@code null}
     * @param foreground the foreground color image; may be {@code null}
     * @param height the height of the output image; if {@code mask} is not {@code null},
     *               the mask’s height must equal this value
     * @param width  the width of the output image; if {@code mask} is not {@code null},
     *               the mask’s width must equal this value
     * @return the composite image
     */
    public static Image composeImage(Pixmap mask, Pixmap background, Pixmap foreground, int height, int width) {
        return composeImage(mask, background, foreground, height, width, ImageRotationType.NO_ROTATION);
    }

    /**
     * Returns a composite image created by painting the foreground color image over the background color image
     * using the foreground mask as a stencil, then applying the specified rotation.
     *
     * @param mask the foreground bitonal mask; may be {@code null}
     * @param background the background color image; may be {@code null}
     * @param foreground the foreground color image; may be {@code null}
     * @param height the height of the output image; if {@code mask} is not {@code null},
     *               the mask’s height must equal this value
     * @param width  the width of the output image; if {@code mask} is not {@code null},
     *               the mask’s width must equal this value
     * @param rotationType the rotation to apply
     * @return the rotated composite image
     */
    public static Image composeImage(Pixmap mask,
                                     Pixmap background,
                                     Pixmap foreground,
                                     int height,
                                     int width,
                                     ImageRotationType rotationType) {
        Image imageMask = toImage(mask, rotationType);
        Image imageBackground = toImage(background, rotationType);

        if (imageBackground == null) {
            return imageMask;
        }
        if (imageMask == null) {
            return imageBackground;
        }

        Image imageForeground = toImage(foreground, rotationType);

        double bgScale = imageBackground.getWidth() / width;
        double fgScale = imageForeground != null ? (imageForeground.getWidth() / width) : -1;
        LOG.debug("bgScale = {}", bgScale);
        LOG.debug("fgScale = {}", fgScale);

        WritableImage image = new WritableImage(width, height);
        PixelWriter writer = image.getPixelWriter();
        PixelReader maskReader = imageMask.getPixelReader();
        PixelReader fgReader = imageForeground == null ? null : imageForeground.getPixelReader();
        PixelReader bgReader = imageBackground.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = maskReader.getColor(x, y);

                Color resultColor = null;
                if (Color.WHITE.equals(color)) {
                    int srcX = Math.min((int) (x * bgScale), (int)(imageBackground.getWidth() - 1));
                    int srcY = Math.min((int) (y * bgScale), (int)(imageBackground.getHeight() - 1));
                    resultColor = bgReader.getColor(srcX, srcY);
                }
                else {
                    if (fgReader != null) {
                        int srcX = Math.min((int) (x * fgScale), (int)(imageForeground.getWidth() - 1));
                        int srcY = Math.min((int) (y * fgScale), (int)(imageForeground.getHeight() - 1));
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

    /**
     * Returns a JB2-decoded image
     *
     * @param data the {@code Sjbz} chunk data
     * @param dict the {@code Djbz} chunk (shape dictionary) data; may be {@code null}
     * @return the JB2-decoded image
     */
    public static JB2Image decodeJB2Image(byte[] data, byte[] dict) {
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

    /**
     * Returns an IW44-decoded image
     *
     * @param data a list of IW44 chunk data that belong to the same parent chunk and have the same IW44 chunk ID
     *             (e.g., {@code BG44}, {@code FG44}, {@code PM44}, {@code BM44}, {@code TH44})
     * @return an IW44-decoded image
     */
    public static IW44Image decodeIW44Image(List<byte[]> data) {
        final IW44Image image = new IW44Image();
        data.forEach(image::decode_chunk);
        image.close_codec();

        return image;
    }

    /**
     * Saves the image to a PNG file.
     *
     * @param image the image to save
     * @param filename the output file name (PNG)
     */
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
