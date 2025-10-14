package com.sternkn.djvu.file.coders;

import java.util.Arrays;
import java.util.stream.IntStream;

public class BSByteStreamUtils {
    // Limits on block sizes
    public static final int KILOBYTE = 1024;
    public static final int MAX_BLOCK = 4096;
    public static final long MAX_BLOCK_SIZE = MAX_BLOCK * KILOBYTE;

    public static final int CTXIDS = 3;

    private static final int[] XMTF = IntStream.range(0, 256).toArray();

    public static int[] getXMTF() {
        return Arrays.copyOf(XMTF, XMTF.length);
    }
}
