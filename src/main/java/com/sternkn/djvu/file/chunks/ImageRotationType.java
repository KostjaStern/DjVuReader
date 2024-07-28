package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.DjVuFileException;

public enum ImageRotationType {

    RIGHT_SIDE_UP(1),         // 0°
    COUNTER_CLOCKWISE_90(6),
    UPSIDE_DOWN(2),           // 180°
    CLOCKWISE_90(5);

    private final int value;

    ImageRotationType(int value) {
        this.value = value;
    }

    public static ImageRotationType getRotationType(byte flag) {
        final int flagValue = flag & 0b0000_0111;
        for (ImageRotationType rotationType : ImageRotationType.values()) {
            if (rotationType.value == flagValue) {
                return rotationType;
            }
        }

        throw new DjVuFileException(String.format("Illegal flag %s in INFO chunk", flag));
    }
}
