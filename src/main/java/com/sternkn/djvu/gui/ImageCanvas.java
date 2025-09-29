package com.sternkn.djvu.gui;

import javax.swing.JToolBar;
import javax.swing.JButton;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ImageCanvas extends Canvas {
    private final BufferedImage image;
    private final JToolBar toolBar;

    private Graphics2D graphics2D;
    private double scale;

    public ImageCanvas(BufferedImage image, JToolBar toolBar) {
        this.image = image;
        this.scale = 0.2;
        this.setSize((int)(scale * image.getWidth()), (int) (scale * image.getHeight()));
        this.toolBar = toolBar;

        addZoomInAction();
        addZoomOutAction();
    }

    private void addZoomInAction() {
        JButton zoomIn = findButtonByName(ToolBarButton.ZOOM_IN.name());
        if (zoomIn == null) {
            return;
        }

        zoomIn.addActionListener(l -> {
            this.scale += 0.01;
            this.revalidate();
            this.repaint();
        });
    }

    private void addZoomOutAction() {
        JButton zoomOut = findButtonByName(ToolBarButton.ZOOM_OUT.name());
        if (zoomOut == null) {
            return;
        }

        zoomOut.addActionListener(l -> {
            this.scale -= 0.01;
            this.revalidate();
            this.repaint();
        });
    }

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);
        graphics2D = (Graphics2D) graphics;
        graphics2D.scale(scale, scale);

        if (image != null) {
            graphics.drawImage(image, 40, 10, this);
        }
    }

    private JButton findButtonByName(String name) {
        for (Component component : toolBar.getComponents()) {
            if (name.equals(component.getName())) {
                return (JButton) component;
            }
        }
        return null;
    }
}
