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
package com.sternkn.djvu.file.chunks;

import org.junit.jupiter.api.Test;

import com.sternkn.djvu.file.coders.TestSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestInfoChunk extends TestSupport {

    @Test
    public void testInfoChunk() {
        Chunk chunk = readChunk("INFO_6.data", ChunkId.INFO);

        InfoChunk infoChunk = new InfoChunk(chunk);

        assertEquals(ChunkId.INFO, infoChunk.getChunkId());
        assertEquals(2832, infoChunk.getWidth());
        assertEquals(4539, infoChunk.getHeight());

        assertEquals(25, infoChunk.getMinorVersion());
        assertEquals(0, infoChunk.getMajorVersion());

        assertEquals(300, infoChunk.getDpi());
        assertEquals(22, infoChunk.getGamma());

        assertEquals(ImageRotationType.NO_ROTATION, infoChunk.getRotation());
    }

    @Test
    public void testInfoChunkWithZeroFlag() {
        Chunk chunk = readChunk("INFO_5_zero_flag.data", ChunkId.INFO);

        InfoChunk infoChunk = new InfoChunk(chunk);

        assertEquals(ChunkId.INFO, infoChunk.getChunkId());
        assertEquals(2550, infoChunk.getWidth());
        assertEquals(3300, infoChunk.getHeight());

        assertEquals(21, infoChunk.getMinorVersion());
        assertEquals(0, infoChunk.getMajorVersion());

        assertEquals(300, infoChunk.getDpi());
        assertEquals(22, infoChunk.getGamma());

        assertEquals(ImageRotationType.NO_ROTATION, infoChunk.getRotation());
    }
}
