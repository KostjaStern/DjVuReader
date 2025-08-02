package com.sternkn.djvu.file.chunks.annotations;

import com.sternkn.djvu.file.chunks.Color;

public class Line extends Area {
    private final Point[] points;
    private boolean hasArrow;
    private int width;
    private Color lineColor;

    public Line(Point[] points) {
        super(AreaType.LINE);
        this.points = points;
    }

    public Line setHasArrow(boolean hasArrow) {
        this.hasArrow = hasArrow;
        return this;
    }
    public boolean hasArrow() {
        return hasArrow;
    }

    public Line setWidth(int width) {
        this.width = width;
        return this;
    }
    public int getWidth() {
        return width;
    }

    public Line setColor(Color color) {
        this.lineColor = color;
        return this;
    }
    public Color getColor() {
        return lineColor;
    }

    public Line setBorder(Border border) {
        this.border = border;
        return this;
    }

    public int getPointsCount() {
        return points.length;
    }

    public Point getPoint(int index) {
        return points[index];
    }

}
