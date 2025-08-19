package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.coders.TestSupport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestFGbzChunk extends TestSupport {

    @Test
    public void testForegroundColorJB2ChunkDecoding() {
        Chunk chunk = readChunk("FGbz_1749.data", ChunkId.FGbz);

        FGbzChunk fgbzChunk = new FGbzChunk(chunk);

        assertEquals(0, fgbzChunk.getVersion());
        assertEquals(1, fgbzChunk.getPaletteSize());
        assertEquals(List.of(Color.BLACK), fgbzChunk.getColors());

        assertEquals(2159, fgbzChunk.getDataSize());
        assertEquals(2159, fgbzChunk.getIndexes().size());

        fgbzChunk.getIndexes().forEach(index -> {
            assertEquals(0, index);
        });
    }
}
