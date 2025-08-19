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
