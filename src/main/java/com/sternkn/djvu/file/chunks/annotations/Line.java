package com.sternkn.djvu.file.chunks.annotations;

import com.sternkn.djvu.file.chunks.Color;

import java.util.Objects;

public class Line extends Area {
    private final Point startPoint;
    private final Point endPoint;
    private boolean hasArrow;
    private int width;
    private Color lineColor;

    public Line(Point startPoint,  Point endPoint) {
        super(AreaType.LINE);
        this.startPoint = startPoint;
        this.endPoint = endPoint;
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

    public Point getStartPoint() {
        return this.startPoint;
    }

    public Point getEndPoint() {
        return this.endPoint;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Line other)) {
            return false;
        }

        return type == other.getType()
                && Objects.equals(this.border, other.border)
                && Objects.equals(this.startPoint, other.startPoint)
                && Objects.equals(this.endPoint, other.endPoint)
                && hasArrow  == other.hasArrow
                && width == other.width
                && Objects.equals(this.lineColor, other.lineColor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, border, startPoint,  endPoint, hasArrow, width, lineColor);
    }

    @Override
    public String toString() {
        return String.format("{type: %s, startPoint: %s, endPoint: %s, hasArrow: %s, width: %s, lineColor: %s, border: %s}",
            type,  startPoint,  endPoint, hasArrow, width, lineColor, border);
    }
}
