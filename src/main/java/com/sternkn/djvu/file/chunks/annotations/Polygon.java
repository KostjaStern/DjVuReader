package com.sternkn.djvu.file.chunks.annotations;

public class Polygon extends Area {

    private final Point[] points;
    private boolean isBorderAlwaysVisible;

    public Polygon(Point[] points) {
        super(AreaType.POLYGON);
        this.points = points;
    }

    public Polygon setBorder(Border border) {
        this.border = border;
        return this;
    }

    public boolean isBorderAlwaysVisible() {
        return isBorderAlwaysVisible;
    }
    public Polygon setBorderAlwaysVisible(boolean isBorderAlwaysVisible) {
        this.isBorderAlwaysVisible = isBorderAlwaysVisible;
        return this;
    }

    public int getPointsCount() {
        return points.length;
    }

    public Point getPoint(int index) {
        return points[index];
    }
}
