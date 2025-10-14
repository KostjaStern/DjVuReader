package com.sternkn.djvu.file.coders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class TestBSByteOutputStream extends TestSupport {

    private ByteArrayOutputStream target;

    @BeforeEach
    public void setUp() {
        target = new ByteArrayOutputStream();
    }

    @Test
    public void testDataEncoding() throws IOException {
        String text = "(maparea \"#463\" \"\" (rect 78 3999 1464 96 ) (border #0000FF ) ) ";

        OutputStream outputStream = new BSByteOutputStream(target, 64);
        outputStream.write(text.getBytes(StandardCharsets.UTF_8));
        outputStream.close();

        byte[] expectedBuffer = readByteBuffer("ANTz_137.data");
        byte[] actualBuffer = target.toByteArray();

        assertArrayEquals(expectedBuffer, actualBuffer);
    }

    @Test
    public void testDIRMChunkDataEncoding() throws IOException {
        InputStream inputStream = readStream("DIRM_2.bzz");
        InputStream decodedStream = readStream("DIRM_decoded.data");

        OutputStream outputStream = new BSByteOutputStream(target, 13);
        outputStream.write(decodedStream.readAllBytes());
        outputStream.close();

        byte[] expectedBuffer = inputStream.readAllBytes();
        byte[] actualBuffer = target.toByteArray();

        assertArrayEquals(expectedBuffer, actualBuffer);

        decodedStream.close();
        inputStream.close();
    }
}
