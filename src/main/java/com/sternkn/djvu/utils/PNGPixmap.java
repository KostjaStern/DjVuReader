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
}
