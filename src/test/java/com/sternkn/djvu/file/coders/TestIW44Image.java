package com.sternkn.djvu.file.coders;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestIW44Image extends TestSupport {

    @Test
    public void testDecodeChunk() {
        byte[] data = readByteBuffer("BG44_test1.data");
        IW44Image image = new IW44Image();
        image.decode_chunk(data);

        assertEquals(792, image.getWidth());
        assertEquals(1115, image.getHeight());
    }
}
