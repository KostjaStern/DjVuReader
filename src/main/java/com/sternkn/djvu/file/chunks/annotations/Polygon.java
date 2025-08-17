package com.sternkn.djvu.file.chunks.annotations;

import java.util.List;
import java.util.Objects;

import static com.sternkn.djvu.file.utils.StringUtils.NL;

public class Polygon extends Area {

    private final List<Point> points;
    private boolean isBorderAlwaysVisible;

    public Polygon(List<Point> points) {
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
        return points.size();
    }

    public Point getPoint(int index) {
        return points.get(index);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Polygon other)) {
            return false;
        }

        return type == other.getType()
                && Objects.equals(this.border, other.border)
                && Objects.equals(this.points, other.points)
                && isBorderAlwaysVisible == other.isBorderAlwaysVisible;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, border, points, isBorderAlwaysVisible);
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("{type: ").append(type).append(",").append(NL);

        int index = 0;
        for (Point point : points) {
            buffer.append(" p").append(index).append(": ")
                  .append(point).append(",").append(NL);
            index++;
        }

        buffer.append(" isBorderAlwaysVisible: ").append(isBorderAlwaysVisible)
              .append(", border: ").append(border).append("}");

        return buffer.toString();
    }
}
