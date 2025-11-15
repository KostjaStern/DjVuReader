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
package com.sternkn.djvu.file.coders;

import java.util.Objects;

public class PixelColor {

    public static final PixelColor WHITE = new PixelColor(255, 255, 255);
    public static final PixelColor BLACK = new PixelColor(0, 0, 0);

    private int blue;
    private int green;
    private int red;

    public PixelColor() {
    }

    public PixelColor(int blue, int green, int red) {
        this.blue = blue;
        this.green = green;
        this.red = red;
    }

    public void setColor(ColorName colorName, int value) {
        switch (colorName) {
            case BLUE:
                blue = value;
                break;
            case GREEN:
                green = value;
                break;
            case RED:
                red = value;
        }
    }

    /*
        {Red, Green, Blue, Alpha}
     */
    public int[] getColor() {
        return new int[]{red, green, blue, 255};
    }

    public int getBlue() {
        return blue;
    }
    public int getGreen() {
        return green;
    }
    public int getRed() {
        return red;
    }

    @Override
    public int hashCode() {
        return Objects.hash(blue, green, red);
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof PixelColor color)) {
            return false;
        }

        return color.getBlue() == blue &&
                color.getGreen() == green &&
                color.getRed() == red;
    }

    @Override
    public String toString() {
        return String.format("{blue: %s, green: %s, red: %s}", blue, green, red);
    }
}
