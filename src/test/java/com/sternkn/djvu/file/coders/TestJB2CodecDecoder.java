package com.sternkn.djvu.file.coders;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

public class TestJB2CodecDecoder {

    private final ClassLoader classLoader = getClass().getClassLoader();

    @Disabled
    @Test
    public void testDecode() throws IOException  {
        try (InputStream inputStream = classLoader.getResourceAsStream("test_chunks/Djbz_4.data")) {
            JB2CodecDecoder decoder = new JB2CodecDecoder(inputStream);
            JB2Dict dict = new JB2Dict();
            decoder.decode(dict);
        }
    }
}
