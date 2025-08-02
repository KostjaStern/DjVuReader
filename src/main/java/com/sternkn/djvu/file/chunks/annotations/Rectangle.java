package com.sternkn.djvu.file.chunks.annotations;

import com.sternkn.djvu.file.chunks.Color;

public class Rectangle extends Area {
    private final int xmin;
    private final int ymin;
    private final int width;
    private final int height;

    private boolean isBorderAlwaysVisible;
    private Color highlightedColor;
    private int opacity; // 0-100, default of 50

    public Rectangle(int xmin, int ymin, int width, int height) {
        super(AreaType.RECTANGLE);

        this.xmin = xmin;
        this.ymin = ymin;
        this.width = width;
        this.height = height;
    }

    public boolean isBorderAlwaysVisible() {
        return isBorderAlwaysVisible;
    }
    public Rectangle setBorderAlwaysVisible(boolean isBorderAlwaysVisible) {
        this.isBorderAlwaysVisible = isBorderAlwaysVisible;
        return this;
    }

    public Rectangle setBorder(Border border) {
        this.border = border;
        return this;
    }
    public Rectangle setOpacity(int opacity) {
        this.opacity = opacity;
        return this;
    }
    public Rectangle setHighlightedColor(Color color) {
        this.highlightedColor = color;
        return this;
    }

    public int getXmin() {
        return xmin;
    }

    public int getYmin() {
        return ymin;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getOpacity() {
        return opacity;
    }

    public Color getHighlightedColor() {
        return highlightedColor;
    }
}
