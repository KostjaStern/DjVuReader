package com.sternkn.djvu.utils.utils;

import com.sternkn.djvu.file.coders.PixelColor;
import com.sternkn.djvu.file.coders.Pixmap;
import javafx.scene.image.Image;
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
}
