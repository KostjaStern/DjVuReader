package com.sternkn.djvu.file.chunks.annotations;

import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Oval other)) {
            return false;
        }

        return type == other.getType()
                && Objects.equals(this.border, other.border)
                && xmin  == other.xmin
                && ymin  == other.ymin
                && width == other.width
                && height == other.height
                && isBorderAlwaysVisible == other.isBorderAlwaysVisible;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, border, xmin,  ymin, width, height, isBorderAlwaysVisible);
    }

    @Override
    public String toString() {
        return String.format("{type: %s, xmin: %s, ymin: %s, width: %s, height: %s, isBorderAlwaysVisible: %s, border: %s}",
                type,  xmin,  ymin, width, height, isBorderAlwaysVisible, border);
    }
}
