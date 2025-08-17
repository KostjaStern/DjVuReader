package com.sternkn.djvu.file.chunks.annotations;

import com.sternkn.djvu.file.chunks.Color;

import java.util.Objects;

import static com.sternkn.djvu.file.utils.StringUtils.NL;

public class Rectangle extends Area {
    private final int xmin;
    private final int ymin;
    private final int width;
    private final int height;

    private boolean isBorderAlwaysVisible;
    private Color highlightedColor;
    private int opacity;

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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Rectangle other)) {
            return false;
        }

        return type == other.getType()
                && Objects.equals(this.border, other.border)
                && this.xmin == other.xmin
                && this.ymin == other.ymin
                && this.width == other.width
                && this.height == other.height
                && isBorderAlwaysVisible == other.isBorderAlwaysVisible
                && Objects.equals(this.highlightedColor, other.highlightedColor)
                && this.opacity == other.opacity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, border, xmin, ymin, width, height, isBorderAlwaysVisible, highlightedColor, opacity);
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("{type: ").append(type)
              .append(", xmin: ").append(xmin)
              .append(", ymin: ").append(ymin)
              .append(", width: ").append(width)
              .append(", height: ").append(height).append(",").append(NL)
              .append(" isBorderAlwaysVisible: ").append(isBorderAlwaysVisible)
              .append(", highlightedColor: ").append(highlightedColor)
              .append(", opacity: ").append(opacity)
              .append(", border: ").append(border).append("}");
        return buffer.toString();
    }
}
