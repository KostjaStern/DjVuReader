package com.sternkn.djvu.file.chunks;

public enum ImageRotationType {

    NO_ROTATION(1),          // 0°
    COUNTER_CLOCKWISE_90(6),
    UPSIDE_DOWN(2),          // 180°
    CLOCKWISE_90(5);

    private final int value;

    ImageRotationType(int value) {
        this.value = value;
    }

    public static ImageRotationType getRotationType(int flag) {
        final int flagValue = flag & 0x7;

        return switch (flagValue) {
            case 6 -> COUNTER_CLOCKWISE_90;
            case 2 -> UPSIDE_DOWN;
            case 5 -> CLOCKWISE_90;
            default -> NO_ROTATION;
        };
    }

    public int getValue() {
        return value;
    }
}
