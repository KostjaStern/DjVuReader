package com.sternkn.djvu.file;

import com.sternkn.djvu.file.chunks.Chunk;
import com.sternkn.djvu.file.chunks.ChunkId;
import com.sternkn.djvu.file.chunks.SecondaryChunkId;
import com.sternkn.djvu.file.coders.TestSupport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestDjVuFile extends TestSupport {

    @Test
    public void testFindSharedShapeChunkWithIllegalArgument() {
        DjVuFile file = new DjVuFile(MagicHeader.AT_T, List.of(), 5000000L);

        Chunk chunk = createChunk(ChunkId.Djbz, null, 2009660L);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> file.findSharedShapeChunk(chunk));
        assertEquals("Chunk id Djbz is not a JB2 bitonal data chunk", exception.getMessage());
    }

    @Test
    public void testFindSharedShapeChunk() {
        Chunk root = createChunk(ChunkId.FORM, SecondaryChunkId.DJVM, 12L);
        Chunk dir = readChunk("DIRM_1.data", ChunkId.DIRM, root, 24L);

        Chunk formDict1 = createChunk(ChunkId.FORM, SecondaryChunkId.DJVI, root, 4560L);
        Chunk dict1 = createChunk(ChunkId.Djbz, null, formDict1, 4572L);

        Chunk formDict2 = createChunk(ChunkId.FORM, SecondaryChunkId.DJVI, root, 2009648L);
        Chunk dict2 = createChunk(ChunkId.Djbz, null, formDict2, 2009660L);

        Chunk formPage1 = createChunk(ChunkId.FORM, SecondaryChunkId.DJVU, root, 2143064L);
        Chunk link = readChunk("INCL_66.data", ChunkId.INCL, formPage1, 2143138L);
        Chunk page1 = createChunk(ChunkId.Sjbz, null, formPage1, 2143162L);

        List<Chunk> chunks = List.of(root,  dir, formDict1, dict1, formDict2, dict2, link, page1);
        DjVuFile file = new DjVuFile(MagicHeader.AT_T, chunks, 5000000L);

        Chunk chunk = file.findSharedShapeChunk(page1);
        assertEquals(dict2,  chunk);
    }

    @Test
    public void testNotFoundSharedShapeChunk() {
        Chunk root = createChunk(ChunkId.FORM, SecondaryChunkId.DJVM, 12L);
        Chunk dir = readChunk("DIRM_with_shared_annotation.data", ChunkId.DIRM, root, 24L);

        Chunk formDict = createChunk(ChunkId.FORM, SecondaryChunkId.DJVI, root, 6312L);
        Chunk ant = createChunk(ChunkId.ANTz, null, formDict, 6324L);

        Chunk formPage1 = createChunk(ChunkId.FORM, SecondaryChunkId.DJVU, root, 68522L);
        Chunk incl = readChunk("INCL_shared_anno.data", ChunkId.INCL, formPage1, 68552L);
        Chunk page1 = createChunk(ChunkId.Sjbz, null, formPage1, 68576L);

        List<Chunk> chunks = List.of(root,  dir, formDict, ant, incl, page1);
        DjVuFile file = new DjVuFile(MagicHeader.AT_T, chunks, 5000000L);

        Chunk chunk = file.findSharedShapeChunk(page1);
        assertNull(chunk);
    }

    private Chunk createChunk(ChunkId chunkId, SecondaryChunkId secondaryId, Long offsetStart) {
        return createChunk(chunkId, secondaryId, null, offsetStart);
    }

    private Chunk createChunk(ChunkId chunkId, SecondaryChunkId secondaryId, Chunk chunk, Long offsetStart) {
        return Chunk.builder()
                .withChunkId(chunkId)
                .withSecondaryChunkId(secondaryId)
                .withParent(chunk)
                .withSize(10L)
                .withOffsetStart(offsetStart)
                .build();
    }
}
