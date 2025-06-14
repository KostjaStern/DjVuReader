package com.sternkn.djvu.file.utils;

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
