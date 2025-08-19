package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.coders.TestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTXTzChunk extends TestSupport {

    @Test
    public void testTextChunkDecoding() {
        Chunk chunk = readChunk("TXTz_10.data", ChunkId.TXTz);

        TXTzChunk textChunk = new TXTzChunk(chunk);
        assertEquals(ChunkId.TXTz, textChunk.getChunkId());
        assertEquals(239, textChunk.getLenText());
        assertEquals(1, textChunk.getVersion());
        assertEquals(1, textChunk.getTextZones().size());

        String expectedText = "Эрик Эеанс \nПредисловие \nМартина Фаулера \nПредметно-ориентированное \n" +
                "проектирование \nСТРУКТУРИЗАЦИЯ СЛОЖНЫХ \nПРОГРАММНЫХ СИСТЕМ \n";
        assertEquals(expectedText, textChunk.getText());

        TextZone pageTextZone = textChunk.getTextZones().getFirst();

        assertTextZone(pageTextZone,
                       TextZoneType.PAGE,
                       new GRect(0, 0, 3956, 5575),
                7, 0, 239);
        assertTextZone(pageTextZone.getChildren().getFirst(),
                TextZoneType.LINE,
                new GRect(280, 5203, 1552, 5459),
                2, 0, 21);

        assertTextZone(pageTextZone.getChildren().getFirst().getChildren().getFirst(),
                TextZoneType.WORD,
                new GRect(280, 5203, 820, 5459),
                0, 0, 9);
        assertTextZone(pageTextZone.getChildren().getFirst().getChildren().getLast(),
                TextZoneType.WORD,
                new GRect(900, 5259, 1552, 5459),
                0, 9, 11);

        assertTextZone(pageTextZone.getChildren().getLast(),
                TextZoneType.LINE,
                new GRect(1476, 2699, 3808, 2891),
                2, 202, 37);
    }

    @Test
    public void testOneMoreTextChunkDecoding() {
        Chunk chunk = readChunk("TXTz_69.data", ChunkId.TXTz);

        TXTzChunk textChunk = new TXTzChunk(chunk);
        assertEquals(ChunkId.TXTz, textChunk.getChunkId());
        assertEquals(1, textChunk.getVersion());
        assertEquals(45, textChunk.getLenText());
        assertEquals(6, textChunk.getTextZoneCount());
        assertEquals(1, textChunk.getTextZones().size());

        String expectedText = "Посвящается маме и папе \n";
        assertEquals(expectedText, textChunk.getText());

        TextZone pageTextZone = textChunk.getTextZones().getFirst();

        assertTextZone(pageTextZone,
                TextZoneType.PAGE,
                new GRect(0, 0, 3699, 5318),
                1, 0, 45);

        TextZone firstLineTextZone = pageTextZone.getChildren().getFirst();
        assertTextZone(firstLineTextZone,
                TextZoneType.LINE,
                new GRect(1267, 4409, 2429, 4502),
                4, 0, 45);

        assertTextZone(firstLineTextZone.getChildren().get(0),
                TextZoneType.WORD,
                new GRect(1267, 4409, 1873, 4502),
                0, 0, 23);
        assertTextZone(firstLineTextZone.getChildren().get(1),
                TextZoneType.WORD,
                new GRect(1895, 4429, 2125, 4480),
                0, 23, 9);
        assertTextZone(firstLineTextZone.getChildren().get(2),
                TextZoneType.WORD,
                new GRect(2151, 4430, 2201, 4479),
                0, 32, 3);
        assertTextZone(firstLineTextZone.getChildren().get(3),
                TextZoneType.WORD,
                new GRect(2227, 4429, 2429, 4480),
                0, 35, 9);
    }

    @Test
    public void testTextChunkWithCharacterZoneDecoding() {
        Chunk chunk = readChunk("TXTz_33.data", ChunkId.TXTz);

        TXTzChunk textChunk = new TXTzChunk(chunk);
        assertEquals(ChunkId.TXTz, textChunk.getChunkId());
        assertEquals(56, textChunk.getLenText());
        assertEquals(1, textChunk.getVersion());
        assertEquals(1, textChunk.getTextZones().size());

        String expectedText = "ЕВГЕНИЙ НОРИН\nЧЕЧЕНСКАЯ ВОИНА\n";
        assertEquals(expectedText, textChunk.getText());

        TextZone pageTextZone = textChunk.getTextZones().getFirst();

        assertTextZone(pageTextZone,
                TextZoneType.PAGE,
                new GRect(375, 4168, 1457, 4494),
                6, 0, 56);

        assertTextZone(pageTextZone.getChildren().get(0),
                TextZoneType.WORD,
                new GRect(377, 4418, 771, 4494),
                0, 0, 14);

        assertTextZone(pageTextZone.getChildren().get(1),
                TextZoneType.CHARACTER,
                new GRect(772, 4418, 812, 4494),
                0, 14, 1);
        assertTextZone(pageTextZone.getChildren().get(2),
                TextZoneType.WORD,
                new GRect(813, 4418, 1131, 4478),
                0, 15, 11);

        assertTextZone(pageTextZone.getChildren().get(3),
                TextZoneType.WORD,
                new GRect(375, 4168, 1005, 4242),
                0, 26, 18);
        assertTextZone(pageTextZone.getChildren().get(4),
                TextZoneType.CHARACTER,
                new GRect(1006, 4170, 1056, 4242),
                0, 44, 1);
        assertTextZone(pageTextZone.getChildren().get(5),
                TextZoneType.WORD,
                new GRect(1057, 4168, 1457, 4242),
                0, 45, 11);
    }

    private void assertTextZone(TextZone textZone,
                                TextZoneType textZoneType,
                                GRect rect,
                                int childrenSize,
                                int textStart,
                                int textLength) {
        assertEquals(textZoneType, textZone.getType());
        assertEquals(rect, textZone.getRect());
        assertEquals(childrenSize, textZone.getChildren().size());
        assertEquals(textStart, textZone.getTextStart());
        assertEquals(textLength, textZone.getTextLength());
    }
}
