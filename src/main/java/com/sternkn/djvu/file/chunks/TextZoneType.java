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
