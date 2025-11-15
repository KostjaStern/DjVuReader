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
