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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestNumberUtils {

    @Test
    public void testAsUnsignedInt() {
        assertEquals(Integer.MAX_VALUE, NumberUtils.asUnsignedInt(Integer.MAX_VALUE));

        assertEquals(0, NumberUtils.asUnsignedInt(0));

        assertEquals(0xFFFFFFFFL, NumberUtils.asUnsignedInt(-1));
    }

    @Test
    public void testAsUnsignedShort() {
        assertEquals(Short.MAX_VALUE, NumberUtils.asUnsignedShort(Short.MAX_VALUE));

        assertEquals(0, NumberUtils.asUnsignedShort(0));

        assertEquals(0xFFFF, NumberUtils.asUnsignedShort(-1));
    }

    @Test
    public void testAsUnsignedByte() {
        assertEquals(Byte.MAX_VALUE, NumberUtils.asUnsignedByte(Byte.MAX_VALUE));

        assertEquals(0, NumberUtils.asUnsignedByte((byte) 0));

        assertEquals(0xFF, NumberUtils.asUnsignedByte((byte) -1));
    }
}
