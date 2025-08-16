package com.sternkn.djvu.file.chunks.annotations;

import com.sternkn.djvu.file.chunks.Color;

import java.util.Objects;

public class Border {
    private boolean none;
    private boolean xor;
    private Color color;
    private Integer shadowIn;
    private Integer shadowOut;
    private Integer shadowEIn;
    private Integer shadowEOut;

    public Border() {
    }

    public boolean isNone() {
        return none;
    }
    public Border setNone(boolean none) {
        this.none = none;
        return this;
    }

    public boolean isXor() {
        return xor;
    }
    public Border setXor(boolean xor) {
        this.xor = xor;
        return this;
    }

    public Color getColor() {
        return color;
    }
    public Border setColor(Color color) {
        this.color = color;
        return this;
    }

    public Integer getShadowIn() {
        return shadowIn;
    }
    public Border setShadowIn(Integer shadowIn) {
        this.shadowIn = shadowIn;
        return this;
    }

    public Integer getShadowOut() {
        return shadowOut;
    }
    public Border setShadowOut(Integer shadowOut) {
        this.shadowOut = shadowOut;
        return this;
    }

    public Integer getShadowEIn() {
        return shadowEIn;
    }
    public Border setShadowEIn(Integer shadowEIn) {
        this.shadowEIn = shadowEIn;
        return this;
    }

    public Integer getShadowEOut() {
        return shadowEOut;
    }
    public Border setShadowEOut(Integer shadowEOut) {
        this.shadowEOut = shadowEOut;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Border other)) {
            return false;
        }

        return none == other.none
                && xor == other.xor
                && Objects.equals(this.color, other.color)
                && Objects.equals(this.shadowIn, other.shadowIn)
                && Objects.equals(this.shadowOut, other.shadowOut)
                && Objects.equals(this.shadowEIn, other.shadowEIn)
                && Objects.equals(this.shadowEOut, other.shadowEOut);
    }

    @Override
    public int hashCode() {
        return Objects.hash(none, xor, color, shadowIn,  shadowOut, shadowEIn, shadowEOut);
    }
}
