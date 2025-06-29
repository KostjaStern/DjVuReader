package com.sternkn.djvu.file.coders;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestJB2CodecDecoder {

    private final ClassLoader classLoader = getClass().getClassLoader();

    @Test
    public void testDecode() throws IOException  {
        try (InputStream inputStream = classLoader.getResourceAsStream("test_chunks/Djbz_4.data")) {
            JB2CodecDecoder decoder = new JB2CodecDecoder(inputStream);
            JB2Dict dict = new JB2Dict();
            decoder.decode(dict);

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
    }
}
