package com.sternkn.djvu.file.chunks;

public enum ComponentType {

    /* file representing a page */
    PAGE,

    /* file containing thumbnails */
    THUMBNAIL,

    /* file included by other files */
    INCLUDED;

    public static ComponentType valueOf(int value) {
        final int flag = value & 0x03;
        return switch (flag) {
            case 0 -> INCLUDED;
            case 1 -> PAGE;
            case 2 -> THUMBNAIL;
            default -> throw new IllegalStateException("Invalid flag: " + value);
        };
    }
}
