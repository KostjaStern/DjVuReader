package com.sternkn.djvu.gui.view;

import javax.swing.JComponent;
import javax.swing.Scrollable;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.Graphics;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ImageCanvas extends JComponent implements Scrollable {
    private static final int INCREMENT_UNIT = 16;

    private final JToolBar toolBar;
    private BufferedImage image;
    private double scale;

    public ImageCanvas(JToolBar toolBar) {
        this.scale = 0.2;
        this.toolBar = toolBar;

        addZoomInAction();
        addZoomOutAction();
    }

    public BufferedImage getImage() {
        return image;
    }
    public void setImage(BufferedImage img) {
        if (image != null) {
            image.flush();
        }
        this.image = img;
    }

    public void rePaint() {
        this.revalidate();
        this.repaint();
    }

    private void addZoomInAction() {
        JButton zoomIn = findButtonByName(ToolBarButton.ZOOM_IN.name());
        if (zoomIn == null) {
            return;
        }

        zoomIn.addActionListener(l -> {
            this.scale += 0.01;
            rePaint();
        });
    }

    private void addZoomOutAction() {
        JButton zoomOut = findButtonByName(ToolBarButton.ZOOM_OUT.name());
        if (zoomOut == null) {
            return;
        }

        zoomOut.addActionListener(l -> {
            this.scale -= 0.01;
            rePaint();
        });
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        if (image != null) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.scale(scale, scale);
            g2.drawImage(image, 40, 10, null);
            g2.dispose();
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

    @Override
    public Dimension getPreferredSize() {
        return image == null ?
            new Dimension(0, 0) :
            new Dimension(
                (int) Math.round(image.getWidth()  * scale),
                (int) Math.round(image.getHeight() * scale));
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public boolean getScrollableTracksViewportWidth()  {
        return false;
    }

    @Override public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle vr, int orient, int dir) {
        return INCREMENT_UNIT;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle vr, int orient, int dir) {
        return orient == SwingConstants.VERTICAL ?
            Math.max(INCREMENT_UNIT, vr.height - INCREMENT_UNIT) :
            Math.max(INCREMENT_UNIT, vr.width  - INCREMENT_UNIT);
    }
}
