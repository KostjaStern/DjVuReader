package com.sternkn.djvu.file.chunks.annotations;

public class Oval extends Area {
    private final int xmin;
    private final int ymin;
    private final int width;
    private final int height;

    private boolean isBorderAlwaysVisible;

    public Oval(int xmin, int ymin, int width, int height) {
        super(AreaType.OVAL);

        this.xmin = xmin;
        this.ymin = ymin;
        this.width = width;
        this.height = height;
    }

    public Oval setBorder(Border border) {
        this.border = border;
        return this;
    }

    public boolean isBorderAlwaysVisible() {
        return isBorderAlwaysVisible;
    }
    public Oval setBorderAlwaysVisible(boolean isBorderAlwaysVisible) {
        this.isBorderAlwaysVisible = isBorderAlwaysVisible;
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
}
