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
    public void testReadANTzChunkWithUrlDecoding() {
        inputStream = readStream("ANTz_10.data");

        BSByteInputStream bsByteInputStream = new BSByteInputStream(inputStream);

        byte[] buffer = bsByteInputStream.readAllBytes();

        String actualData = new String(buffer, StandardCharsets.UTF_8);
        String expectedData = "(maparea \"http://www.libclassicmusic.ru\" \"\" (rect 117 2314 1275 120 ) (border #0000FF ) ) ";

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
