/*
    Copyright (C) 2025 Kostya Stern

    This program is free software; you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation; either version 2 of the License, or (at your option)
    any later version.

    This program is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
    more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc., 51
    Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
*/
package com.sternkn.djvu.file.chunks.annotations;

import java.util.List;
import java.util.Objects;

import static com.sternkn.djvu.utils.StringUtils.NL;

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
