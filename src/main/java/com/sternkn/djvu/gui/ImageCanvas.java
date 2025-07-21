package com.sternkn.djvu.gui;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ImageCanvas extends Canvas {
    private final BufferedImage image;

    public ImageCanvas(BufferedImage image) {
        this.image =  image;
    }

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.scale(0.2, 0.2);

        if (image != null) {
            graphics.drawImage(image, 40, 10, this);
        }
    }
}
