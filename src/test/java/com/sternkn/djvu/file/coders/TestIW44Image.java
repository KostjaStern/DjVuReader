package com.sternkn.djvu.file.coders;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;


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
        GPixmap expectedPixmap = getPixmap();

        assertEquals(expectedPixmap, pixmap);
    }

    private GPixmap getPixmap() {
        try {
            BufferedImage image = ImageIO.read(new File("./src/test/resources/test_images/BG44_test.png"));
            final int width = image.getWidth();
            final int height = image.getHeight();
            GPixmap pixmap = new GPixmap(height, width);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int argb = image.getRGB(x, y);

                    final int red   = (argb >> 16) & 0xFF;
                    final int green = (argb >> 8)  & 0xFF;
                    final int blue  = argb         & 0xFF;
                    PixelColor color = new PixelColor(blue, green, red);
                    pixmap.setPixel(x, y, color);
                }
            }

            return pixmap;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
