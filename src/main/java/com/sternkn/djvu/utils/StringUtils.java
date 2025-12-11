/*
    Copyright (C) 2025 Kostya Stern

    This program is free software; you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation; either version 2 of the License, or (at your option)
    any later version.

    This program is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
    more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc., 51
    Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
*/
package com.sternkn.djvu.utils;

import java.nio.charset.StandardCharsets;

public class StringUtils {
    public static final String NL = System.lineSeparator();

    public static String padRight(Object data, int n) {
        return String.format("%-" + n + "s", data);
    }

    public static String repeatString(String str, int n) {
        return str.repeat(n);
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
