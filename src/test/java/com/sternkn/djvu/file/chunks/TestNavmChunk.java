package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.coders.TestSupport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestNavmChunk extends TestSupport {

    @Test
    public void testNavmChunkDecoding() {
        Chunk chunk = readChunk("NAVM_2.data", ChunkId.NAVM);

        NavmChunk navChunk = new NavmChunk(chunk);
        assertEquals(ChunkId.NAVM, navChunk.getChunkId());
        assertEquals(List.of(
                new Bookmark(10, "Титул", 2, "#1"),
                new Bookmark(18, "Аннотация", 2, "#2"),
                new Bookmark(22, "Предисловие", 2, "#5"),
                new Bookmark(40, "Предисловие от автора", 2, "#8"),
                new Bookmark(35, "Глава 1. Волчий угол", 3, "#12"),
                new Bookmark(45, "Глава 2. Пир стервятников", 3, "#44"),
                new Bookmark(47, "Глава 3. Нулевая чеченская", 3, "#76"),
                new Bookmark(22, "Глава 4. Morituri", 3, "#98"),
                new Bookmark(43, "Глава 5. Град обреченный", 4, "#138"),
                new Bookmark(32, "Глава 6. Чистилище", 4, "#178"),
                new Bookmark(39, "Глава 7. Грязная драка", 4, "#212"),
                new Bookmark(35, "Глава 8. Черная дыра", 4, "#262"),
                new Bookmark(48, "Глава 9. Кулаки после драки", 4, "#312"),
                new Bookmark(33, "Список источников", 4, "#340"),
                new Bookmark(20, "Оглавление", 4, "#350")
        ), navChunk.getBookmarks());
    }

    @Test
    public void testNavmChunkDecodingWithChildBookmarks() {
        Chunk chunk = readChunk("NAVM_1.data", ChunkId.NAVM);

        NavmChunk navChunk = new NavmChunk(chunk);
        assertEquals(ChunkId.NAVM, navChunk.getChunkId());
        assertEquals(235, navChunk.getBookmarks().size());

        assertEquals(new Bookmark(21, "Оглавление ", 2, "#5"),
                     navChunk.getBookmarks().get(0));
        assertEquals(new Bookmark(21, "Содержание ", 2, "#6"),
                navChunk.getBookmarks().get(1));

        assertEquals(new Bookmark(2, 79, "Часть I. Модель предметной области в работе ", 3, "#27"),
                navChunk.getBookmarks().get(6));
        assertEquals(new Bookmark(5, 50, "Глава 1. Переработка знаний ", 3, "#32"),
                navChunk.getBookmarks().get(9));

        assertEquals(new Bookmark(40, "Предметный указатель ", 4, "#425"),
                navChunk.getBookmarks().get(234));
    }
}
