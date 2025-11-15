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

import com.sternkn.djvu.file.coders.TestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestInclChunk extends TestSupport {

    @Test
    public void testInclChunkDecoding() {
        Chunk chunk = readChunk("INCL_15.data", ChunkId.INCL);
        InclChunk inclChunk = new InclChunk(chunk);

        assertEquals(ChunkId.INCL, inclChunk.getChunkId());
        assertEquals("Ab0009_0001.djbz", inclChunk.getSharedComponentID());
    }

    @Test
    public void testOneMoreInclChunkDecoding() {
        Chunk chunk = readChunk("INCL_66.data", ChunkId.INCL);
        InclChunk inclChunk = new InclChunk(chunk);

        assertEquals(ChunkId.INCL, inclChunk.getChunkId());
        assertEquals("Ab0019_0001.djbz", inclChunk.getSharedComponentID());
    }
}
