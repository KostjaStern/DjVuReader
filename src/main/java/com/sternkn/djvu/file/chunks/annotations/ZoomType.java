package com.sternkn.djvu.file.chunks.annotations;

public enum ZoomType {
    STRETCH,
    ONE2ONE,
    WIDTH,
    PAGE;

    public static ZoomType of(String value) {
        for (ZoomType zoom : values()) {
            if (zoom.name().equalsIgnoreCase(value)) {
                return zoom;
            }
        }
        return null;
    }
}
