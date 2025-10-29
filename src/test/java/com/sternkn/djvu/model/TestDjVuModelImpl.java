package com.sternkn.djvu.model;

import com.sternkn.djvu.file.DjVuFile;
import com.sternkn.djvu.file.DjVuFileException;
import com.sternkn.djvu.file.chunks.Chunk;
import com.sternkn.djvu.file.chunks.ChunkId;
import com.sternkn.djvu.file.chunks.GRect;
import com.sternkn.djvu.file.chunks.SecondaryChunkId;
import com.sternkn.djvu.file.chunks.TextZone;
import com.sternkn.djvu.file.chunks.TextZoneType;
import com.sternkn.djvu.file.coders.GPixmap;
import com.sternkn.djvu.file.coders.TestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestDjVuModelImpl extends TestSupport {

    private DjVuFile djvuFile;
    private DjVuModelImpl model;

    @BeforeEach
    public void setUp() {
        djvuFile = mock(DjVuFile.class);
        model = new DjVuModelImpl(djvuFile);
    }

    @AfterEach
    public void tearDown() {

    }

    @Test
    public void testGetChunkStatistics() {
        List<Chunk> chunks = List.of(
            createCompositeChunk(1L, SecondaryChunkId.DJVM),
            createChunk(2L, ChunkId.DIRM),
            createChunk(3L, ChunkId.NAVM),
            createCompositeChunk(4L, SecondaryChunkId.DJVI),
            createChunk(5L, ChunkId.Djbz),
            createCompositeChunk(6L, SecondaryChunkId.DJVU),
            createChunk(7L, ChunkId.INFO),
            createChunk(8L, ChunkId.INCL),
            createChunk(9L, ChunkId.Sjbz),
            createChunk(10L, ChunkId.FG44),
            createChunk(11L, ChunkId.BG44),
            createChunk(12L, ChunkId.BG44),
            createChunk(13L, ChunkId.BG44),
            createChunk(14L, ChunkId.BG44),
            createCompositeChunk(15L, SecondaryChunkId.DJVU),
            createChunk(16L, ChunkId.INFO)
        );

        when(djvuFile.getChunks()).thenReturn(chunks);

        String statistics = model.getChunkStatistics();
        String expectedStatistics = """
            Composite chunks
        ---------------------------------
         FORM:DJVU      : 2
         FORM:DJVI      : 1
         FORM:DJVM      : 1
        
        
            Data chunks
        ---------------------------------
         DIRM           : 1
         FG44           : 1
         Sjbz           : 1
         BG44           : 4
         INCL           : 1
         INFO           : 2
         Djbz           : 1
         NAVM           : 1
        """;
        assertEquals(expectedStatistics, statistics);
    }

    @Test
    public void testSaveChunkData(@TempDir File tempDir) throws IOException {
        File file = new File(tempDir, "chunk.data");
        String data = "Ab0009_0001.djbz";
        byte[] bytes = data.getBytes();

        Chunk chunk = Chunk.builder()
                .withChunkId(ChunkId.INCL)
                .withId(23L)
                .withData(bytes)
                .withSize(bytes.length)
                .build();

        when(djvuFile.getChunks()).thenReturn(List.of(chunk));

        model.saveChunkData(file, 23L);

        String actualData = Files.readString(file.toPath());
        assertEquals(data, actualData);
    }

    @Test
    public void testSaveChunkDataNotFound() {
        File file = mock(File.class);

        when(djvuFile.getChunks()).thenReturn(List.of());

        Exception exception = assertThrows(DjVuFileException.class, () -> model.saveChunkData(file, 23L));
        assertEquals("Chunk with id 23 not found", exception.getMessage());
    }

    @Test
    public void testGetChunkInfoForIW44Chunk() {
        Chunk chunk1 = createChunk(1L, ChunkId.BG44, "BG44_test1.data");
        Chunk chunk2 = createChunk(2L, ChunkId.BG44, "BG44_test2.data");
        Chunk chunk3 = createChunk(3L, ChunkId.BG44, "BG44_test3.data");
        List<Chunk> chunks = List.of(chunk1, chunk2, chunk3);

        when(djvuFile.getChunks()).thenReturn(chunks);
        when(djvuFile.getAllImageChunks(chunk1)).thenReturn(chunks);

        ChunkInfo chunkInfo = model.getChunkInfo(1L);
        GPixmap expectedPixmap = readPixmap("BG44_test.png");
        String expectedTextData = """
          ChunkId: BG44
         OffsetStart: 0
         OffsetEnd: 26143
         Size: 26143
        
         majorVersion = 1
         minorVersion = 2
         colorType = 0
         chrominanceDelay = 10
         crcbHalf = 0
         height = 1115
         width = 792
        """;

        assertEquals(1L, chunkInfo.getChunkId());
        assertPixmapEquals(expectedPixmap, chunkInfo.getBitmap());
        assertEquals(expectedTextData, chunkInfo.getTextData());
        assertNull(chunkInfo.getTextZones());
        assertEquals(0, chunkInfo.getTextZoneCount());
    }

    @Test
    public void testGetChunkInfoForBitonalChunk() {
        Chunk djbz = createChunk(1L, ChunkId.Djbz, "Mozart_Djbz.data");
        Chunk sjbz = createChunk(2L, ChunkId.Sjbz, "Mozart_Sjbz.data");
        List<Chunk> chunks = List.of(djbz, sjbz);

        when(djvuFile.getChunks()).thenReturn(chunks);
        when(djvuFile.findSharedShapeChunk(sjbz)).thenReturn(djbz);

        ChunkInfo chunkInfo = model.getChunkInfo(2L);
        GPixmap expectedPixmap = readPixmap("Mozart.png");
        String expectedTextData = """
         ChunkId: Sjbz
         OffsetStart: 0
         OffsetEnd: 249179
         Size: 249179
        """;

        assertEquals(2L, chunkInfo.getChunkId());
        assertPixmapEquals(expectedPixmap, chunkInfo.getBitmap());
        assertEquals(expectedTextData, chunkInfo.getTextData());
        assertNull(chunkInfo.getTextZones());
        assertEquals(0, chunkInfo.getTextZoneCount());
    }

    @Test
    public void testGetChunkInfoForTextChunk() {
        Chunk textChunk = createChunk(2L, ChunkId.TXTz, "TXTz_10.data");

        when(djvuFile.getChunks()).thenReturn(List.of(textChunk));

        ChunkInfo chunkInfo = model.getChunkInfo(2L);

        String expectedTextData = """
         ChunkId: TXTz
         OffsetStart: 0
         OffsetEnd: 304
         Size: 304
        
         Version: 1
         Text zone count: 19
         Size of the text string in bytes: 239
         Text:\s
        --------------------------------------------------------
        Эрик Эеанс\s
        Предисловие\s
        Мартина Фаулера\s
        Предметно-ориентированное\s
        проектирование\s
        СТРУКТУРИЗАЦИЯ СЛОЖНЫХ\s
        ПРОГРАММНЫХ СИСТЕМ\s
        
        
        """;

        assertEquals(2L, chunkInfo.getChunkId());
        assertNull(chunkInfo.getBitmap());
        assertEquals(expectedTextData, chunkInfo.getTextData());
        assertEquals(1, chunkInfo.getTextZones().size());
        assertEquals(19, chunkInfo.getTextZoneCount());

        TextZone textZone = chunkInfo.getTextZones().getFirst();
        assertEquals(TextZoneType.PAGE, textZone.getType());
        assertEquals(0, textZone.getTextStart());
        assertEquals(239, textZone.getTextLength());
        assertEquals(new GRect(0, 0, 3956, 5575), textZone.getRect());
    }

    @Test
    public void testGetChunkInfo() {
        String data = "Ab0009_0001.djbz";
        byte[] bytes = data.getBytes();

        Chunk chunk = Chunk.builder()
                .withChunkId(ChunkId.INCL)
                .withId(23L)
                .withData(bytes)
                .withSize(bytes.length)
                .build();

        when(djvuFile.getChunks()).thenReturn(List.of(chunk));

        ChunkInfo chunkInfo = model.getChunkInfo(23L);

        String expectedTextData = """
         ChunkId: INCL
         OffsetStart: 0
         OffsetEnd: 16
         Size: 16
        
         Shared component ID: Ab0009_0001.djbz
        """;

        assertEquals(23L, chunkInfo.getChunkId());
        assertNull(chunkInfo.getBitmap());
        assertEquals(expectedTextData, chunkInfo.getTextData());
        assertNull(chunkInfo.getTextZones());
        assertEquals(0, chunkInfo.getTextZoneCount());
    }

    private Chunk createCompositeChunk(long id, SecondaryChunkId secondaryChunkId) {
        return Chunk.builder()
                .withChunkId(ChunkId.FORM)
                .withSecondaryChunkId(secondaryChunkId)
                .withId(id)
                .withSize(1)
                .build();
    }

    private Chunk createChunk(long id, ChunkId chunkId) {
        return Chunk.builder()
                .withChunkId(chunkId)
                .withId(id)
                .withSize(1)
                .build();
    }

    private Chunk createChunk(long id, ChunkId chunkId, String dataFile) {
        byte[] data = readByteBuffer(dataFile);
        return Chunk.builder()
                .withChunkId(chunkId)
                .withId(id)
                .withData(data)
                .withSize(data.length)
                .build();
    }
}
