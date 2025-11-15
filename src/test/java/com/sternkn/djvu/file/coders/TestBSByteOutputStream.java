/*
    Copyright (C) 2025 Kostya Stern

    This program is free software; you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation; either version 2 of the License, or (at your option)
    any later version.

    This program is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
    more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc., 51
    Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
*/
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
