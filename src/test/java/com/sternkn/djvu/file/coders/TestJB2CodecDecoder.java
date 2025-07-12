package com.sternkn.djvu.file.coders;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestJB2CodecDecoder {

    private final ClassLoader classLoader = getClass().getClassLoader();

    @Test
    public void testDecodeImageWithoutDictionary() {
        JB2Image image = new JB2Image();

        try (InputStream inputStream = classLoader.getResourceAsStream("test_chunks/sample1_Sjbz_9.data")) {
            JB2CodecDecoder decoder = new JB2CodecDecoder(inputStream);
            decoder.decode(image);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertEquals(2600, image.getWidth());
        assertEquals(4200, image.getHeight());
        assertEquals(1861, image.get_blit_count());
        assertEquals(120, image.get_shape_count());
        assertEquals(0, image.get_inherited_shape_count());

        assertEquals(new JB2Blit(1539, 4049, 0), image.get_blit(0));
        assertEquals(new JB2Blit(1569, 4048, 1), image.get_blit(1));
        assertEquals(new JB2Blit(1596, 4048, 2), image.get_blit(2));
        assertEquals(new JB2Blit(1800, 4029, 117), image.get_blit(1858));
        assertEquals(new JB2Blit(2100, 4029, 118), image.get_blit(1859));
        assertEquals(new JB2Blit(2400, 4029, 119), image.get_blit(1860));
    }

    @Test
    public void testDecodeImage() {
        JB2Dict dict = readDictionary("Djbz_4.data");
        JB2Image image = new JB2Image(dict);

        try (InputStream inputStream = classLoader.getResourceAsStream("test_chunks/Sjbz_16.data")) {
            JB2CodecDecoder decoder = new JB2CodecDecoder(inputStream);
            decoder.decode(image);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertEquals(2832, image.getWidth());
        assertEquals(4539, image.getHeight());
        assertEquals(241, image.get_blit_count());
        assertEquals(707, image.get_shape_count());
        assertEquals(488, image.get_inherited_shape_count());

        assertEquals(new JB2Blit(1285, 4025, 488), image.get_blit(0));
        assertEquals(new JB2Blit(1329, 4011, 489), image.get_blit(1));
        assertEquals(new JB2Blit(1840, 4017, 490), image.get_blit(2));
        assertEquals(new JB2Blit(2000, 62, 704), image.get_blit(238));
        assertEquals(new JB2Blit(1000, 65, 705), image.get_blit(239));
        assertEquals(new JB2Blit(1500, 66, 706), image.get_blit(240));
    }

    @Test
    public void testDecodeDictionary() {
        JB2Dict dict = readDictionary("Djbz_4.data");

        assertEquals(488, dict.get_shape_count());

        assertEquals(34, dict.get_shape(0).getBits().columns());
        assertEquals(41, dict.get_shape(0).getBits().rows());
        assertEquals(32, dict.get_shape(1).getBits().columns());
        assertEquals(38, dict.get_shape(1).getBits().rows());

        assertEquals(5, dict.get_shape(486).getBits().columns());
        assertEquals(7, dict.get_shape(486).getBits().rows());
        assertEquals(22, dict.get_shape(487).getBits().columns());
        assertEquals(21, dict.get_shape(487).getBits().rows());
    }

    private JB2Dict readDictionary(String fileName) {
        JB2Dict dict = new JB2Dict();

        try (InputStream inputStream = classLoader.getResourceAsStream("test_chunks/" + fileName)) {
            JB2CodecDecoder decoder = new JB2CodecDecoder(inputStream);
            decoder.decode(dict);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        return dict;
    }
}
