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
