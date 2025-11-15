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
package com.sternkn.djvu.file.coders;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestIW44Image extends TestSupport {

    @Test
    public void testDecodeBackgroundChunk() {
        byte[] data1 = readByteBuffer("BG44_test1.data");
        byte[] data2 = readByteBuffer("BG44_test2.data");
        byte[] data3 = readByteBuffer("BG44_test3.data");

        IW44Image image = new IW44Image();
        image.decode_chunk(data1);
        image.decode_chunk(data2);
        image.decode_chunk(data3);
        image.close_codec();

        assertEquals(792, image.getWidth());
        assertEquals(1115, image.getHeight());

        GPixmap pixmap = image.get_pixmap();
        GPixmap expectedPixmap = readPixmap("BG44_test.png");

        assertEquals(expectedPixmap, pixmap);
    }

    @Test
    public void testDecodeForegroundChunk() {
        byte[] data = readByteBuffer("FG44_9.data");

        IW44Image image = new IW44Image();
        image.decode_chunk(data);
        image.close_codec();

        assertEquals(293, image.getWidth());
        assertEquals(433, image.getHeight());

        GPixmap pixmap = image.get_pixmap();
        GPixmap expectedPixmap = readPixmap("FG44_test.png");

        assertEquals(expectedPixmap, pixmap);
    }
}
