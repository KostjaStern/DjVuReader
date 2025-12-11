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

import com.sternkn.djvu.file.chunks.Color;

import java.util.Objects;

import static com.sternkn.djvu.utils.StringUtils.NL;

public class Text extends Area {
    private final int xmin;
    private final int ymin;
    private final int width;
    private final int height;

    private Color backgroundColor;
    private Color textColor;
    private boolean pushPin;

    public Text(int xmin, int ymin, int width, int height) {
        super(AreaType.TEXT_BOX);
        this.xmin = xmin;
        this.ymin = ymin;
        this.width = width;
        this.height = height;
    }

    public Text setBackgroundColor(Color color) {
        this.backgroundColor = color;
        return this;
    }
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public Text setTextColor(Color color) {
        this.textColor = color;
        return this;
    }
    public Color getTextColor() {
        return textColor;
    }

    public Text setPushPin(boolean pushPin) {
        this.pushPin = pushPin;
        return this;
    }
    public boolean isPushPin() {
        return pushPin;
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
        if (!(obj instanceof Text other)) {
            return false;
        }

        return type == other.getType()
                && Objects.equals(this.border, other.border)
                && this.xmin == other.xmin
                && this.ymin == other.ymin
                && this.width == other.width
                && this.height == other.height
                && Objects.equals(this.backgroundColor, other.backgroundColor)
                && Objects.equals(this.textColor, other.textColor)
                && this.pushPin == other.pushPin;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, border, xmin, ymin, width, height, backgroundColor, textColor, pushPin);
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("{type: ").append(type)
              .append(", xmin: ").append(xmin)
              .append(", ymin: ").append(ymin)
              .append(", width: ").append(width)
              .append(", height: ").append(height).append(",").append(NL)
              .append(" backgroundColor: ").append(backgroundColor)
              .append(", textColor: ").append(textColor)
              .append(", pushPin: ").append(pushPin)
              .append(", border: ").append(border).append("}");
        return buffer.toString();
    }
}
