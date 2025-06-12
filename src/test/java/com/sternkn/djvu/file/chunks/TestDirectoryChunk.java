package com.sternkn.djvu.file.chunks;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestDirectoryChunk {

    private final ClassLoader classLoader = getClass().getClassLoader();

    @Test
    public void testDirectoryChunkDecoding() throws IOException {
        try (InputStream inputStream = classLoader.getResourceAsStream("test_chunks/DIRM_1.data")) {

            byte[] buffer = inputStream.readAllBytes();

            Chunk chunk = Chunk.builder()
                    .withChunkId(ChunkId.DIRM)
                    .withData(new ByteArrayInputStream(buffer))
                    .withSize(buffer.length).build();

            DirectoryChunk directoryChunk = new DirectoryChunk(chunk);

            assertEquals(1, directoryChunk.getVersion());
            assertTrue(directoryChunk.isBundled());
            assertEquals(588, directoryChunk.getNumberOfComponents());

            List<ComponentInfo> components = directoryChunk.getComponents();

            assertEquals(588, components.size());

            assertEquals(new ComponentInfo()
                    .setOffset(4552)
                    .setSize(11175)
                    .setFlag(0)
                    .setId("Ab0009_0001.djbz"), components.get(0));
            assertEquals(new ComponentInfo()
                    .setOffset(15728)
                    .setSize(1407727)
                    .setFlag(1)
                    .setId("Обложка.djvu"), components.get(1));
            assertEquals(new ComponentInfo()
                    .setOffset(1423456)
                    .setSize(249285)
                    .setFlag(1)
                    .setId("Ab0001_0001.djvu"), components.get(2));

            assertEquals(new ComponentInfo()
                    .setOffset(29424654)
                    .setSize(10901)
                    .setFlag(1)
                    .setId("Ab0532_0001.djvu"), components.get(586));
            assertEquals(new ComponentInfo()
                    .setOffset(29435556)
                    .setSize(16313)
                    .setFlag(1)
                    .setId("Oglav0001_0001.djvu"), components.get(587));
        }
    }
}
