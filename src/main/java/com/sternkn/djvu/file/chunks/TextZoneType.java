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

import java.util.Arrays;

/**
  These constants are used to tell what a zone describes.
  This can be useful for a copy/paste application.
  The deeper we go into the hierarchy, the higher the constant.
 */
public enum TextZoneType {
    PAGE(1),
    COLUMN(2),
    REGION(3),
    PARAGRAPH(4),
    LINE(5),
    WORD(6),
    CHARACTER(7);

    private final int code;

    TextZoneType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static TextZoneType valueOf(int value) {
        return Arrays.stream(TextZoneType.values())
            .filter(type -> type.code == value)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Invalid TextZoneType code: " + value));
    }
}
