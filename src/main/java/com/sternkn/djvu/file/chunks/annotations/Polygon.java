package com.sternkn.djvu.file.chunks.annotations;

import java.util.Arrays;
import java.util.Objects;

public class Polygon extends Area {

    private final Point[] points;
    private boolean isBorderAlwaysVisible;

    public Polygon(Point[] points) {
        super(AreaType.POLYGON);
        this.points = points;
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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Polygon other)) {
            return false;
        }

        return type == other.getType()
                && Objects.equals(this.border, other.border)
                && Arrays.equals(this.points, other.points)
                && isBorderAlwaysVisible == other.isBorderAlwaysVisible;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, border, Arrays.hashCode(points), isBorderAlwaysVisible);
    }
}
