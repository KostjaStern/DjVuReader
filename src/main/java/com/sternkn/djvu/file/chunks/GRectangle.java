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
package com.sternkn.djvu.file.chunks;

public record GRectangle(int xmin, int ymin, int xmax, int ymax) {

    public GRectangle {
        if (xmin > xmax) {
            throw new IllegalArgumentException("xmin must be less or equal to xmax");
        }

        if (ymin > ymax) {
            throw new IllegalArgumentException("ymin must be less or equal to ymax");
        }
    }

    public GRectangle(double x1, double y1, double x2, double y2) {
        this((int) Math.floor(Math.min(x1, x2)),
             (int) Math.floor(Math.min(y1, y2)),
             (int) Math.ceil(Math.max(x1, x2)),
             (int) Math.ceil(Math.max(y1, y2)));
    }

    public int getWidth() {
        return xmax - xmin;
    }

    public int getHeight() {
        return ymax - ymin;
    }

    public boolean isEmpty() {
        return (xmin >= xmax) || (ymin >= ymax);
    }

    public int area() {
        return isEmpty() ? 0 : getWidth() * getHeight();
    }

    public boolean isOverlapped(GRectangle rectangle) {
        return this.xmin < rectangle.xmax() && this.xmax > rectangle.xmin()
                && this.ymin < rectangle.ymax() && this.ymax > rectangle.ymin();
    }
}
