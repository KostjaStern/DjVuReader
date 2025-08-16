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
        return String.format("Color{blue: %s, green: %s, red: %s, alpha: %s}", blue, green, red, alpha);
    }
}
