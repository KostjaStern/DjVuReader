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

import java.util.Objects;

public class Color {
    public static final Color BLACK = new Color(0, 0, 0);

    private static final int RMUL = 5;
    private static final int GMUL = 9;
    private static final int BMUL = 2;
    private static final int SMUL = (RMUL+GMUL+BMUL);

    private final int blue;
    private final int green;
    private final int red;
    private final int alpha;

    public Color(int blue, int green, int red) {
        this.blue = blue;
        this.green = green;
        this.red = red;
        this.alpha = (blue * BMUL + green * GMUL + red * RMUL)/SMUL;
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

    public int getAlpha() {
        return alpha;
    }

    @Override
    public int hashCode() {
        return Objects.hash(blue, green, red, alpha);
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof Color color)) {
            return false;
        }

        return color.getBlue() == blue &&
               color.getGreen() == green &&
               color.getRed() == red &&
               color.getAlpha() == alpha;
    }

    @Override
    public String toString() {
        return String.format("{blue: %s, green: %s, red: %s, alpha: %s}", blue, green, red, alpha);
    }
}
