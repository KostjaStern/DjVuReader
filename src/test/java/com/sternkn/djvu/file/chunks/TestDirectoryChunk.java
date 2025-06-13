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

            assertEquals(createComponent(4552, 11175, 0, "Ab0009_0001.djbz"),
                         components.get(0));
            assertEquals(createComponent(15728, 1407727, 1, "Обложка.djvu"),
                         components.get(1));
            assertEquals(createComponent(1423456, 249285, 1, "Ab0001_0001.djvu"),
                         components.get(2));

            assertEquals(createComponent(29424654, 10901, 1, "Ab0532_0001.djvu"),
                         components.get(586));
            assertEquals(createComponent(29435556, 16313, 1, "Oglav0001_0001.djvu"),
                         components.get(587));
        }
    }

    @Test
    public void testOneMoreDirectoryChunkDecoding() throws IOException {
        try (InputStream inputStream = classLoader.getResourceAsStream("test_chunks/DIRM_2.data")) {

            byte[] buffer = inputStream.readAllBytes();

            Chunk chunk = Chunk.builder()
                    .withChunkId(ChunkId.DIRM)
                    .withData(new ByteArrayInputStream(buffer))
                    .withSize(buffer.length).build();

            DirectoryChunk directoryChunk = new DirectoryChunk(chunk);

            assertEquals(1, directoryChunk.getVersion());
            assertTrue(directoryChunk.isBundled());
            assertEquals(291, directoryChunk.getNumberOfComponents());

            List<ComponentInfo> components = directoryChunk.getComponents();

            assertEquals(291, components.size());

            assertEquals(createComponent(2228, 159007, 1, "File146_0001.djvu"),
                         components.get(0));
            assertEquals(createComponent(161236, 106667, 0, "0288.djbz"),
                         components.get(1));
            assertEquals(createComponent(267904, 14278, 1, "0001.djvu"),
                         components.get(2));

            assertEquals(createComponent(3165822, 70933, 1, "0287.djvu"),
                    components.get(288));
            assertEquals(createComponent(3236756, 169165, 1, "0288.djvu"),
                    components.get(289));
            assertEquals(createComponent(3405922, 251751, 1, "File147_0001.djvu"),
                    components.get(290));
        }
    }

    private ComponentInfo createComponent(long offset, int size, int flag, String id) {
        return new ComponentInfo()
                .setOffset(offset)
                .setSize(size)
                .setFlag(flag)
                .setId(id);
    }
}
