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
