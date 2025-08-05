package com.sternkn.djvu.file.chunks.annotations;

public enum ModeType {
    COLOR,
    BW,
    FORE,
    BLACK;

    public static ModeType of(String value) {
        for (ModeType modeType : values()) {
            if (modeType.name().equalsIgnoreCase(value)) {
                return modeType;
            }
        }
        return null;
    }
}
