package com.sternkn.djvu.file.utils;

public class StringUtils {
    public static final String NL = System.lineSeparator();

    public static String padRight(Object data, int n) {
        return String.format("%-" + n + "s", data);
    }

    public static String padLeft(Object data, int n) {
        return String.format("%" + n + "s", data);
    }
}
