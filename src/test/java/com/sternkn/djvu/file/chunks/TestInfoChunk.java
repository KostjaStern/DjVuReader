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

        assertEquals(ImageRotationType.RIGHT_SIDE_UP, infoChunk.getRotation());
    }
}
