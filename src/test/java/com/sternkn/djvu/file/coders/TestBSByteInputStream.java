package com.sternkn.djvu.file.coders;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestBSByteInputStream extends TestSupport {

    private InputStream inputStream;

    @AfterEach
    public void tearDown() throws IOException {
        inputStream.close();
    }

    @Test
    public void testReadNullBuffer() {
        inputStream = readStream("ANTz_137.data");

        BSByteInputStream bsByteInputStream = new BSByteInputStream(inputStream);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> bsByteInputStream.read(null));
        assertEquals("buffer can not be null", exception.getMessage());
    }

    @Test
    public void testReadANTzChunkDecoding() {
        inputStream = readStream("ANTz_137.data");

        BSByteInputStream bsByteInputStream = new BSByteInputStream(inputStream);

        byte[] buffer = bsByteInputStream.readAllBytes();

        String actualData = new String(buffer, StandardCharsets.UTF_8);
        String expectedData = "(maparea \"#463\" \"\" (rect 78 3999 1464 96 ) (border #0000FF ) ) ";

        assertEquals(expectedData, actualData);
    }

    @Test
    public void testReadBiggerANTzChunkDecoding() {
        inputStream = readStream("ANTz_293.data");

        BSByteInputStream bsByteInputStream = new BSByteInputStream(inputStream);
        byte[] buffer = bsByteInputStream.readAllBytes();

        String actualData = new String(buffer, StandardCharsets.UTF_8);
        String expectedData = "(maparea \"#465\" \"\" (rect 84 3811 1240 88 ) (border #0000FF ) ) " +
                              "(maparea \"#466\" \"\" (rect 236 3019 640 76 ) (border #0000FF ) ) " +
                              "(maparea \"#466\" \"\" (rect 960 124 1708 88 ) (border #0000FF ) ) ";

        assertEquals(expectedData, actualData);
    }

    @Test
    public void testReadDIRMChunkDecoding() throws IOException {
        inputStream = readStream("DIRM_2.bzz");

        try (InputStream decodedStream = readStream("DIRM_decoded.data")) {

            BSByteInputStream bsByteInputStream = new BSByteInputStream(inputStream);

            byte[] data = bsByteInputStream.readAllBytes();
            byte[] expectedData = decodedStream.readAllBytes();

            assertArrayEquals(expectedData, data);
        }
    }
}
