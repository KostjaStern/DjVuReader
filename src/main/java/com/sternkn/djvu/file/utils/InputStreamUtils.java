package com.sternkn.djvu.file.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.sternkn.djvu.file.utils.NumberUtils.asUnsignedByte;

public final class InputStreamUtils {
    private InputStreamUtils() {
    }

    public static int read16(InputStream data) {
        try {
            byte[] int16 = new byte[2];
            data.read(int16);

            int b1 = asUnsignedByte(int16[0]);
            int b2 = asUnsignedByte(int16[1]);

            return (b1 << 8) + b2;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int read24(InputStream data) {
        try {
            byte[] int24 = new byte[3];
            data.read(int24);

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
            data.read(int32);

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
