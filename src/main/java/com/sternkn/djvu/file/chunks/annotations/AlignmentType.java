package com.sternkn.djvu.file.chunks.annotations;

public enum AlignmentType {
    LEFT,
    CENTER,
    RIGHT,
    TOP,
    BOTTOM;

    public static AlignmentType of(String value) {
        for (AlignmentType type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
}
