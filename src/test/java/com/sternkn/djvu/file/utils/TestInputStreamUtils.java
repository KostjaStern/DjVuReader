package com.sternkn.djvu.file.utils;

import com.sternkn.djvu.file.DjVuFileException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static com.sternkn.djvu.file.utils.InputStreamUtils.read16;
import static com.sternkn.djvu.file.utils.InputStreamUtils.read24;
import static com.sternkn.djvu.file.utils.InputStreamUtils.read32;
import static com.sternkn.djvu.file.utils.InputStreamUtils.readZeroTerminatedString;

public class TestInputStreamUtils {

    @Test
    public void testRead16() {
        // 257 -> 0x0101
        assertEquals(257, read16(buildStream(new byte[]{1, 1})));

        // 511 -> 0x01FF
        assertEquals(511, read16(buildStream(new byte[]{1, -1})));

        // 65281 -> 0xFF01
        assertEquals(65281, read16(buildStream(new byte[]{-1, 1})));

        // 65535 -> 0xFFFF
        assertEquals(65535, read16(buildStream(new byte[]{-1, -1})));
    }

    @Test
    public void testRead16UnexpectedLength() {
        assertThrows(DjVuFileException.class, () -> read16(buildStream(new byte[]{1})));
    }

    @Test
    public void testRead24() {
        // 65793 -> 0x010101
        assertEquals(65793, read24(buildStream(new byte[]{1, 1, 1})));

        // 66047 -> 0x0101FF
        assertEquals(66047, read24(buildStream(new byte[]{1, 1, -1})));

        // 130817 -> 0x01FF01
        assertEquals(130817, read24(buildStream(new byte[]{1, -1, 1})));

        // 16711937 -> 0xFF0101
        assertEquals(16711937, read24(buildStream(new byte[]{-1, 1, 1})));

        // 16777215 -> 0xFFFFFF
        assertEquals(16777215, read24(buildStream(new byte[]{-1, -1, -1})));
    }

    @Test
    public void testRead24UnexpectedLength() {
        assertThrows(DjVuFileException.class, () -> read24(buildStream(new byte[]{1, 2})));
    }

    @Test
    public void testRead32() {
        // 16843009 -> 0x01010101
        assertEquals(16843009, read32(buildStream(new byte[]{1, 1, 1, 1})));

        // 16843263 -> 0x010101FF
        assertEquals(16843263, read32(buildStream(new byte[]{1, 1, 1, -1})));

        // 16908033 -> 0x0101FF01
        assertEquals(16908033, read32(buildStream(new byte[]{1, 1, -1, 1})));

        // 33489153 -> 0x01FF0101
        assertEquals(33489153, read32(buildStream(new byte[]{1, -1, 1, 1})));

        // 4278255873 -> 0xFF010101
        assertEquals(4278255873L, read32(buildStream(new byte[]{-1, 1, 1, 1})));

        // 4294967295 -> 0xFFFFFFFF
        assertEquals(4294967295L, read32(buildStream(new byte[]{-1, -1, -1, -1})));
    }

    @Test
    public void testRead32UnexpectedLength() {
        assertThrows(DjVuFileException.class, () -> read32(buildStream(new byte[]{1, 2, 3})));
    }

    @Test
    public void testReadZeroTerminatedString() {
        byte[] data = {72, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100, 0, 108, 108, 108};
        String string = readZeroTerminatedString(buildStream(data));
        assertEquals("Hello world", string);
    }

    private InputStream buildStream(byte[] data) {
        return new ByteArrayInputStream(data);
    }
}
