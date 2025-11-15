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
