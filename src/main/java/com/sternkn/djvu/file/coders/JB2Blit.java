package com.sternkn.djvu.file.coders;

import java.util.Objects;

/**
 * Blit data structure. A {JB2Image} contains an array of {JB2Blit} data structures.
 * Each array entry instructs the decoder to render a particular shape at a particular location.
 * Members {left} and {bottom} specify the coordinates of the bottom left corner of the shape bitmap.
 * All coordinates are relative to the bottom left corner of the image.
 * Member {shapeno} is the subscript of the shape to be rendered.
 */
public class JB2Blit implements Parent {

    /** Horizontal coordinate of the blit. */
    private int left;

    /** Vertical coordinate of the blit. */
    private int bottom;

    /** Index of the shape to blit. */
    private int shapeno;

    public JB2Blit() {
        this.left = 0;
        this.bottom = 0;
        this.shapeno = 0;
    }

    public JB2Blit(int left, int bottom, int shapeno) {
        this.left = left;
        this.bottom = bottom;
        this.shapeno = shapeno;
    }

    public int getLeft() {
        return left;
    }
    public void setLeft(int left) {
        this.left = left;
    }

    public int getBottom() {
        return bottom;
    }
    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public int getShapeno() {
        return shapeno;
    }
    public void setShapeno(int shapeno) {
        this.shapeno = shapeno;
    }

    @Override
    public void setParent(int parent) {
        this.shapeno = parent;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof JB2Blit other)) {
            return false;
        }

        return Objects.equals(this.bottom, other.bottom)
            && Objects.equals(this.left, other.left)
            && Objects.equals(this.shapeno, other.shapeno);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bottom, left, shapeno);
    }

    @Override
    public String toString() {
        return "JB2Blit{left = " + left +
                ", bottom = " + bottom +
                ", shapeno = " + shapeno + "}";
    }
}
