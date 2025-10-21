package com.sternkn.djvu.gui.view_model;

import com.sternkn.djvu.file.chunks.TextZone;

import java.util.Objects;

public class TextZoneNode {
    private final TextZone textZone;

    public TextZoneNode(TextZone textZone) {
        this.textZone = textZone;
    }

    public TextZone getTextZone() {
        return textZone;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TextZoneNode other)) {
            return false;
        }
        return Objects.equals(textZone, other.textZone);
    }

    @Override
    public int hashCode() {
        return textZone == null ? 0 : textZone.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(textZone.getType().name());
        buffer.append(":");
        buffer.append(textZone.getRect());
        buffer.append(" [textStart = ");
        buffer.append(textZone.getTextStart());
        buffer.append(", textLength = ");
        buffer.append(textZone.getTextLength());
        buffer.append("]");

        return buffer.toString();
    }
}
