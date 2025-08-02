package com.sternkn.djvu.file.utils;

import java.nio.charset.StandardCharsets;

public class StringUtils {
    public static final String NL = System.lineSeparator();

    public static String padRight(Object data, int n) {
        return String.format("%-" + n + "s", data);
    }

    public static String padLeft(Object data, int n) {
        return String.format("%" + n + "s", data);
    }

    /**
     *
     * @param text - UTF-8 string
     * @return UTF-16 string
     */
    public static String toUTF16(String text) {
        return new String(text.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_16);
    }

    /**
     *
     * @param text - UTF-16 string
     * @param index - the index to the {@code char} values
     * @return character as string
     */
    public static String getChar(String text, int index) {
        final int codePoint = text.codePointAt(index);
        return new String(Character.toChars(codePoint));
    }
}
