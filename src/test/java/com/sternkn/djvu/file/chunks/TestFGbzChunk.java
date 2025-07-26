package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.coders.TestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestFGbzChunk extends TestSupport {
    private InputStream inputStream;

    @AfterEach
    public void tearDown() throws IOException {
        inputStream.close();
    }

    @Test
    public void testForegroundColorJB2ChunkDecoding() throws IOException {
        inputStream = readStream("FGbz_1749.data");
        byte[] buffer = inputStream.readAllBytes();

        Chunk chunk = Chunk.builder()
                .withChunkId(ChunkId.FGbz)
                .withData(buffer)
                .withSize(buffer.length).build();

        FGbzChunk fgbzChunk = new FGbzChunk(chunk);

        assertEquals(0, fgbzChunk.getVersion());
        assertEquals(1, fgbzChunk.getPaletteSize());
        assertEquals(List.of(new Color(0, 0, 0)), fgbzChunk.getColors());

        assertEquals(2159, fgbzChunk.getDataSize());
        assertEquals(2159, fgbzChunk.getIndexes().size());

        fgbzChunk.getIndexes().forEach(index -> {
            assertEquals(0, index);
        });
    }
}
