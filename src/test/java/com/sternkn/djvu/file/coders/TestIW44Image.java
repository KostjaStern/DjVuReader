package com.sternkn.djvu.file.coders;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestIW44Image extends TestSupport {

    @Test
    public void testDecodeChunk() {
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
        assertNotNull(pixmap);

        // int blue, int green, int red
        assertEquals(new PixelColor(151, 193, 188), pixmap.getPixel(0, 0));
        assertEquals(new PixelColor(136, 177, 175), pixmap.getPixel(1, 0));
        assertEquals(new PixelColor(120, 163, 161), pixmap.getPixel(2, 0));
        assertEquals(new PixelColor(115, 157, 157), pixmap.getPixel(3, 0));
        assertEquals(new PixelColor(112, 154, 154), pixmap.getPixel(4, 0));

        assertEquals(new PixelColor(123, 165, 160), pixmap.getPixel(0, 1));
        assertEquals(new PixelColor(99, 142, 140), pixmap.getPixel(1, 1));
        assertEquals(new PixelColor(86, 131, 129), pixmap.getPixel(2, 1));
        assertEquals(new PixelColor(78, 122, 122), pixmap.getPixel(3, 1));
        assertEquals(new PixelColor(74, 120, 120), pixmap.getPixel(4, 1));
        assertEquals(new PixelColor(77, 122, 125), pixmap.getPixel(5, 1));
        assertEquals(new PixelColor(83, 128, 131), pixmap.getPixel(6, 1));

        assertEquals(new PixelColor(182, 121, 22), pixmap.getPixel(782, 1114));
        assertEquals(new PixelColor(184, 123, 24), pixmap.getPixel(783, 1114));
        assertEquals(new PixelColor(186, 125, 26), pixmap.getPixel(784, 1114));
        assertEquals(new PixelColor(186, 125, 26), pixmap.getPixel(785, 1114));
        assertEquals(new PixelColor(186, 125, 26), pixmap.getPixel(786, 1114));
        assertEquals(new PixelColor(186, 125, 26), pixmap.getPixel(790, 1114));
        assertEquals(new PixelColor(186, 125, 26), pixmap.getPixel(791, 1114));

//        assertEquals(new PixelColor(251, 251, 251), pixmap.getPixel(392, 557));
//        assertEquals(new PixelColor(253, 253, 253), pixmap.getPixel(393, 557));
//        assertEquals(new PixelColor(255, 255, 255), pixmap.getPixel(394, 557));
    }
}
