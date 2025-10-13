package com.sternkn.djvu.file.coders;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class TestBSByteOutputStream extends TestSupport {

    @Test
    public void testDataEncoding() {
        String text = "(maparea \"#463\" \"\" (rect 78 3999 1464 96 ) (border #0000FF ) ) ";
        ByteArrayOutputStream target = new ByteArrayOutputStream();

        try {
            OutputStream outputStream = new BSByteOutputStream(target, 64);
            outputStream.write(text.getBytes(StandardCharsets.UTF_8));
            outputStream.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] expectedBuffer = readByteBuffer("ANTz_137.data");
        byte[] actualBuffer = target.toByteArray();

        assertArrayEquals(expectedBuffer, actualBuffer);
    }
}
