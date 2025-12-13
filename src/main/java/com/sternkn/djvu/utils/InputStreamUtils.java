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
package com.sternkn.djvu.utils;

import com.sternkn.djvu.file.DjVuFileException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.sternkn.djvu.utils.NumberUtils.asUnsignedByte;

public final class InputStreamUtils {
    private InputStreamUtils() {
    }

    public static int read8(InputStream data) {
        try {
            return data.read();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int read16(InputStream data) {
        return read16(data, ByteOrder.BIG_ENDIAN);
    }

    public static int read16(InputStream data, ByteOrder byteOrder) {
        try {
            byte[] int16 = new byte[2];
            validateLength(data.read(int16), int16.length);

            int b1 = asUnsignedByte(int16[0]);
            int b2 = asUnsignedByte(int16[1]);

            if (byteOrder == ByteOrder.BIG_ENDIAN) {
                return (b1 << 8) + b2;
            }
            else {
                return (b2 << 8) + b1;
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int read24(InputStream data) {
        try {
            byte[] int24 = new byte[3];
            validateLength(data.read(int24), int24.length);

            int b1 = asUnsignedByte(int24[0]);
            int b2 = asUnsignedByte(int24[1]);
            int b3 = asUnsignedByte(int24[2]);

            return (b1 << 16) + (b2 << 8) + b3;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static long read32(InputStream data) {
        try {
            byte[] int32 = new byte[4];
            validateLength(data.read(int32) , int32.length);

            long b1 = asUnsignedByte(int32[0]);
            long b2 = asUnsignedByte(int32[1]);
            long b3 = asUnsignedByte(int32[2]);
            long b4 = asUnsignedByte(int32[3]);

            return (b1 << 24) + (b2 << 16) + (b3 << 8) + b4;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void validateLength(int length, int expectedLength) {
        if (length != expectedLength) {
            throw new DjVuFileException("Unexpected end of file");
        }
    }

    public static String readString(InputStream data, int stringLength) {
        try {
            byte[] buffer = new byte[stringLength];
            validateLength(data.read(buffer) , buffer.length);

            return new String(buffer, StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readString(InputStream data) {
        try {
            byte[] buffer = data.readAllBytes();
            return new String(buffer, StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readZeroTerminatedString(InputStream data) {
        try {
            int b = data.read();
            List<Integer> bytes = new ArrayList<>();
            while (b != 0 && b != -1) {
                bytes.add(b);
                b = data.read();
            }

            byte[] buffer = new byte[bytes.size()];
            for (int ind = 0; ind < bytes.size(); ind++) {
                buffer[ind] = (byte) (bytes.get(ind) & 0xFF);
            }

            return new String(buffer, StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
