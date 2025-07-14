package com.sternkn.djvu.file.chunks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.sternkn.djvu.file.coders.TestSupport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestInfoChunk extends TestSupport {

    private InputStream inputStream;
    private byte[] buffer;

    @BeforeEach
    public void setUp() throws IOException {
        inputStream = readStream("INFO_6.data");
        buffer = inputStream.readAllBytes();
    }

    @AfterEach
    public void tearDown() throws IOException {
        inputStream.close();
        buffer = null;
    }

    @Test
    public void testInfoChunk() {
        Chunk chunk = Chunk.builder()
                .withChunkId(ChunkId.INFO)
                .withData(new ByteArrayInputStream(buffer))
                .withSize(buffer.length).build();

        InfoChunk infoChunk = new InfoChunk(chunk);

        assertEquals(ChunkId.INFO, infoChunk.getChunkId());
        assertEquals(2832, infoChunk.getWidth());
        assertEquals(4539, infoChunk.getHeight());

        assertEquals(25, infoChunk.getMinorVersion());
        assertEquals(0, infoChunk.getMajorVersion());

        assertEquals(11265, infoChunk.getDpi());
        assertEquals(22, infoChunk.getGamma());

        assertEquals(ImageRotationType.RIGHT_SIDE_UP, infoChunk.getRotation());
    }
}
