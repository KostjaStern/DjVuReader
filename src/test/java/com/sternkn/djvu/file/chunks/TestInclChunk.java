package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.coders.TestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestInclChunk extends TestSupport {
    private InputStream inputStream;

    @AfterEach
    public void tearDown() throws IOException {
        inputStream.close();
    }

    @Test
    public void testInclChunkDecoding() throws IOException {
        inputStream = readStream("INCL_15.data");
        InclChunk inclChunk = buildInclChunk(inputStream);

        assertEquals(ChunkId.INCL, inclChunk.getChunkId());
        assertEquals("Ab0009_0001.djbz", inclChunk.getSharedComponentID());
    }

    @Test
    public void testOneMoreInclChunkDecoding() throws IOException {
        inputStream = readStream("INCL_66.data");
        InclChunk inclChunk = buildInclChunk(inputStream);

        assertEquals(ChunkId.INCL, inclChunk.getChunkId());
        assertEquals("Ab0019_0001.djbz", inclChunk.getSharedComponentID());
    }

    private InclChunk buildInclChunk(InputStream inputStream) throws IOException {
        byte[] buffer = inputStream.readAllBytes();

        Chunk chunk = Chunk.builder()
                .withChunkId(ChunkId.INCL)
                .withData(buffer)
                .withSize(buffer.length).build();

        return new InclChunk(chunk);
    }
}
