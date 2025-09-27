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
