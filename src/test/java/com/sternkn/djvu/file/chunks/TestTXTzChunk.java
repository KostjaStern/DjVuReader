package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.coders.TestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTXTzChunk extends TestSupport {
    private InputStream inputStream;

    @AfterEach
    public void tearDown() throws IOException {
        inputStream.close();
    }

    @Test
    public void testTextChunkDecoding() throws IOException {
        inputStream = readStream("TXTz_912.data");
        byte[] buffer = inputStream.readAllBytes();

        Chunk chunk = Chunk.builder()
                .withChunkId(ChunkId.TXTz)
                .withData(buffer)
                .withSize(buffer.length).build();

        TXTzChunk textChunk = new TXTzChunk(chunk);
        assertEquals(ChunkId.TXTz, textChunk.getChunkId());
        assertEquals(3720, textChunk.getLenText());
        assertEquals(1, textChunk.getVersion());
        assertEquals(1, textChunk.getTextZones().size());

        TextZone firstTextZone = textChunk.getTextZones().getFirst();
        assertEquals(TextZoneType.PAGE, firstTextZone.getType());
        assertEquals(new GRect(0, 0, 2842, 4545), firstTextZone.getRect());
        assertEquals(38, firstTextZone.getChildren().size());
    }
}
