package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.coders.TestSupport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestDirectoryChunk extends TestSupport {

    @Test
    public void testDirectoryChunkDecoding() {
        Chunk chunk = readChunk("DIRM_1.data", ChunkId.DIRM);

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

    @Test
    public void testOneMoreDirectoryChunkDecoding() {
        Chunk chunk = readChunk("DIRM_2.data", ChunkId.DIRM);

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

    @Test
    public void testDirectoryChunkWithSharedAnnotation() {
        Chunk chunk = readChunk("DIRM_with_shared_annotation.data", ChunkId.DIRM);

        DirectoryChunk directoryChunk = new DirectoryChunk(chunk);

        assertEquals(1, directoryChunk.getVersion());
        assertTrue(directoryChunk.isBundled());
        assertEquals(387, directoryChunk.getNumberOfComponents());

        List<ComponentInfo> components = directoryChunk.getComponents();

        assertEquals(387, components.size());

        assertEquals(createComponent(6304, 876, 3, "shared_anno.iff"),
                components.get(0));
        assertEquals(createComponent(7180, 61333, 65, "nb0001.djvu", "C1"),
                components.get(1));
        assertEquals(createComponent(68514, 14866, 65, "nb0002.djvu", "1"),
                components.get(2));

        assertEquals(createComponent(39526846, 82481, 65, "nb0384.djvu", "383"),
                components.get(384));
        assertEquals(createComponent(39609328, 127400, 65, "nb0385.djvu", "384"),
                components.get(385));
        assertEquals(createComponent(39736728, 124369, 65, "nb0386.djvu", "C2"),
                components.get(386));
    }

    private ComponentInfo createComponent(long offset, int size, int flag, String id) {
        return new ComponentInfo()
                .setOffset(offset)
                .setSize(size)
                .setFlag(flag)
                .setId(id);
    }

    private ComponentInfo createComponent(long offset, int size, int flag, String id, String title) {
        return new ComponentInfo()
                .setOffset(offset)
                .setSize(size)
                .setFlag(flag)
                .setId(id)
                .setTitle(title);
    }
}
