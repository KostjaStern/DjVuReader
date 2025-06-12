package com.sternkn.djvu.file.utils;

public final class NumberUtils {
    private NumberUtils() {
    }

    public static long asUnsignedInt(long value) {
        return value & 0xFFFFFFFFL;
    }

    public static long asUnsignedShort(long value) {
        return value & 0xFFFFL;
    }

    public static int asUnsignedByte(byte value) {
        return value & 0xFF;
    }
}
