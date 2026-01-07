/*
    Copyright (C) 2025 Kostya Stern

    This program is free software; you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation; either version 2 of the License, or (at your option)
    any later version.

    This program is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
    more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc., 51
    Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
*/
package com.sternkn.djvu.file;

import com.sternkn.djvu.file.chunks.Chunk;
import com.sternkn.djvu.file.chunks.ChunkId;
import com.sternkn.djvu.file.chunks.NavmChunk;
import com.sternkn.djvu.file.chunks.SecondaryChunkId;
import com.sternkn.djvu.file.coders.TestSupport;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestDjVuFileImpl extends TestSupport {

    @Test
    public void testFindSharedShapeChunkWithIllegalArgument() {
        Chunk root = createChunk(1L, ChunkId.FORM, SecondaryChunkId.DJVM, 12L);
        Chunk dir = readChunk(2L, "DIRM_1.data", ChunkId.DIRM, root, 24L);

        DjVuFile file = new DjVuFileImpl(List.of(root, dir));

        Chunk chunk = createChunk(3L, ChunkId.Djbz, null, 2009660L);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> file.findSharedShapeChunk(chunk));
        assertEquals("Chunk id Djbz is not a JB2 bitonal data chunk", exception.getMessage());
    }

    @Test
    public void testFindSharedShapeChunk() {
        Chunk root = createChunk(1L, ChunkId.FORM, SecondaryChunkId.DJVM, 12L);
        Chunk dir = readChunk(2L, "DIRM_1.data", ChunkId.DIRM, root, 24L);

        Chunk formDict1 = createChunk(3L, ChunkId.FORM, SecondaryChunkId.DJVI, root, 4560L);
        Chunk dict1 = createChunk(4L, ChunkId.Djbz, null, formDict1, 4572L);

        Chunk formDict2 = createChunk(5L, ChunkId.FORM, SecondaryChunkId.DJVI, root, 2009648L);
        Chunk dict2 = createChunk(6L, ChunkId.Djbz, null, formDict2, 2009660L);

        Chunk formPage1 = createChunk(7L, ChunkId.FORM, SecondaryChunkId.DJVU, root, 2143064L);
        Chunk link = readChunk(8L, "INCL_66.data", ChunkId.INCL, formPage1, 2143138L);
        Chunk page1 = createChunk(9L, ChunkId.Sjbz, null, formPage1, 2143162L);

        List<Chunk> chunks = List.of(root,  dir, formDict1, dict1, formDict2, dict2, link, page1);
        DjVuFile file = new DjVuFileImpl(chunks);

        Chunk chunk = file.findSharedShapeChunk(page1);
        assertEquals(dict2,  chunk);
    }

    @Test
    public void testFindSiblingSharedShapeChunk() {
        Chunk root = createChunk(1L, ChunkId.FORM, SecondaryChunkId.DJVM, 12L);
        Chunk dir = readChunk(2L, "Akunin_DIRM.data", ChunkId.DIRM, root, 24L);

        Chunk formPage = createChunk(3L, ChunkId.FORM, SecondaryChunkId.DJVU, root, 1746L);
        Chunk dict = createChunk(4L, ChunkId.Djbz, null, formPage, 1776L);
        Chunk page = createChunk(5L, ChunkId.Sjbz, null, formPage, 6152L);

        List<Chunk> chunks = List.of(root,  dir, formPage, dict, page);
        DjVuFile file = new DjVuFileImpl(chunks);

        Chunk chunk = file.findSharedShapeChunk(page);
        assertEquals(dict,  chunk);
    }

    @Test
    public void testFindSharedShapeChunkForMultipleInclCase() {
        Chunk root = createChunk(1L, ChunkId.FORM, SecondaryChunkId.DJVM, 12L);
        Chunk dir = readChunk(2L, "DIRM_mult_INCL_case.data", ChunkId.DIRM, root, 24L);

        Chunk formWrapper = createChunk(3L, ChunkId.FORM, SecondaryChunkId.DJVI, root, 1718L);

        Chunk form1 = createChunk(4L, ChunkId.FORM, SecondaryChunkId.DJVI, formWrapper, 108144L);

        Chunk dict = createChunk(5L, ChunkId.Djbz, null, form1, 108156L);

        Chunk form2 = createChunk(6L, ChunkId.FORM, SecondaryChunkId.DJVU, formWrapper, 185416L);
        Chunk link1 = readChunk(7L, "INCL_mult_INCL_case_1.data", ChunkId.INCL, form2, 185446L);
        Chunk link2 = readChunk(8L, "INCL_mult_INCL_case_2.data", ChunkId.INCL, form2, 185470L);
        Chunk page1 = createChunk(9L, ChunkId.Sjbz, null, form2, 185492L);

        List<Chunk> chunks = List.of(root,  dir, formWrapper, form1, dict, form2, link1, link2, page1);
        DjVuFile file = new DjVuFileImpl(chunks);

        Chunk chunk = file.findSharedShapeChunk(page1);
        assertEquals(dict,  chunk);
    }

    @Test
    public void testNotFoundSharedShapeChunk() {
        Chunk root = createChunk(1L, ChunkId.FORM, SecondaryChunkId.DJVM, 12L);
        Chunk dir = readChunk(2L, "DIRM_with_shared_annotation.data", ChunkId.DIRM, root, 24L);

        Chunk formDict = createChunk(3L, ChunkId.FORM, SecondaryChunkId.DJVI, root, 6312L);
        Chunk ant = createChunk(4L, ChunkId.ANTz, null, formDict, 6324L);

        Chunk formPage1 = createChunk(5L, ChunkId.FORM, SecondaryChunkId.DJVU, root, 68522L);
        Chunk incl = readChunk(6L, "INCL_shared_anno.data", ChunkId.INCL, formPage1, 68552L);
        Chunk page1 = createChunk(7L, ChunkId.Sjbz, null, formPage1, 68576L);

        List<Chunk> chunks = List.of(root,  dir, formDict, ant, incl, page1);
        DjVuFile file = new DjVuFileImpl(chunks);

        Chunk chunk = file.findSharedShapeChunk(page1);
        assertNull(chunk);
    }

    @Test
    public void testGetChunkById() {
        Chunk root = createChunk(1L, ChunkId.FORM, SecondaryChunkId.DJVM, 12L);
        Chunk dir = readChunk(2L, "DIRM_with_shared_annotation.data", ChunkId.DIRM, root, 24L);

        Chunk formDict = createChunk(3L, ChunkId.FORM, SecondaryChunkId.DJVI, root, 6312L);
        Chunk ant = createChunk(4L, ChunkId.ANTz, null, formDict, 6324L);

        Chunk formPage1 = createChunk(5L, ChunkId.FORM, SecondaryChunkId.DJVU, root, 68522L);
        Chunk incl = readChunk(6L, "INCL_shared_anno.data", ChunkId.INCL, formPage1, 68552L);
        Chunk page1 = createChunk(7L, ChunkId.Sjbz, null, formPage1, 68576L);

        List<Chunk> chunks = List.of(root,  dir, formDict, ant, incl, page1);
        DjVuFile file = new DjVuFileImpl(chunks);

        Chunk chunk = file.getChunkById(4L);
        assertEquals(ant,  chunk);
    }

    @Test
    public void testGetChunkByIdNotFound() {
        Chunk root = createChunk(1L, ChunkId.FORM, SecondaryChunkId.DJVM, 12L);
        Chunk dir = readChunk(2L, "DIRM_with_shared_annotation.data", ChunkId.DIRM, root, 24L);

        List<Chunk> chunks = List.of(root,  dir);
        DjVuFile file = new DjVuFileImpl(chunks);

        final long chunkId = 4L;
        final String error = String.format("Chunk with id %s not found.", chunkId);

        Exception exception = assertThrows(DjVuFileException.class, () -> file.getChunkById(chunkId));
        assertEquals(error, exception.getMessage());
    }

    @Test
    public void testGetChunkByOffset() {
        Chunk root = createChunk(1L, ChunkId.FORM, SecondaryChunkId.DJVM, 12L);
        Chunk dir = readChunk(2L, "DIRM_with_shared_annotation.data", ChunkId.DIRM, root, 24L);

        Chunk formDict = createChunk(3L, ChunkId.FORM, SecondaryChunkId.DJVI, root, 6312L);
        Chunk ant = createChunk(4L, ChunkId.ANTz, null, formDict, 6324L);

        List<Chunk> chunks = List.of(root,  dir, formDict, ant);
        DjVuFile file = new DjVuFileImpl(chunks);

        Chunk chunk = file.getChunkByOffset(6304L);
        assertEquals(ant,  chunk);
    }

    @Test
    public void testGetChunkByOffsetNotFound() {
        Chunk root = createChunk(1L, ChunkId.FORM, SecondaryChunkId.DJVM, 12L);
        Chunk dir = readChunk(2L, "DIRM_with_shared_annotation.data", ChunkId.DIRM, root, 24L);

        Chunk formDict = createChunk(3L, ChunkId.FORM, SecondaryChunkId.DJVI, root, 6312L);
        Chunk ant = createChunk(4L, ChunkId.ANTz, null, formDict, 6324L);

        List<Chunk> chunks = List.of(root,  dir, formDict, ant);
        DjVuFile file = new DjVuFileImpl(chunks);

        final long offset = 6306L;
        final String error = String.format("Chunk with offset %s not found.", offset);

        Exception exception = assertThrows(DjVuFileException.class, () -> file.getChunkByOffset(offset));
        assertEquals(error, exception.getMessage());
    }

    @Test
    public void testGetNavigationMenuEmpty() {
        Chunk root = createChunk(1L, ChunkId.FORM, SecondaryChunkId.DJVM, 12L);
        Chunk dir = readChunk(2L, "DIRM_with_shared_annotation.data", ChunkId.DIRM, root, 24L);
        Chunk formDict = createChunk(3L, ChunkId.FORM, SecondaryChunkId.DJVI, root, 6312L);

        List<Chunk> chunks = List.of(root,  dir, formDict);
        DjVuFile file = new DjVuFileImpl(chunks);

        Optional<NavmChunk> navMenu = file.getNavigationMenu();

        assertTrue(navMenu.isEmpty());
    }

    @Test
    public void testGetNavigationMenu() {
        Chunk root = createChunk(1L, ChunkId.FORM, SecondaryChunkId.DJVM, 12L);
        Chunk dir = readChunk(2L, "DIRM_with_shared_annotation.data", ChunkId.DIRM, root, 24L);
        Chunk nav = readChunk(3L, "Tanimura_NAVM.data", ChunkId.NAVM, root, 24L);
        Chunk formDict = createChunk(4L, ChunkId.FORM, SecondaryChunkId.DJVI, root, 6312L);

        List<Chunk> chunks = List.of(root,  dir, nav, formDict);
        DjVuFile file = new DjVuFileImpl(chunks);

        Optional<NavmChunk> navMenu = file.getNavigationMenu();

        assertTrue(navMenu.isPresent());
        assertEquals(new NavmChunk(nav), navMenu.get());
    }

    @Test
    public void testGetAllPageChunks() {
        Chunk root = createChunk(1L, ChunkId.FORM, SecondaryChunkId.DJVM, 12L);
        Chunk dir = readChunk(2L, "DIRM_with_shared_annotation.data", ChunkId.DIRM, root, 24L);

        Chunk page = createChunk(3L, ChunkId.FORM, SecondaryChunkId.DJVU, root, 68522L);
        Chunk info = createChunk(4L, ChunkId.INFO, null, page, 68576L);
        Chunk incl = readChunk(5L, "INCL_shared_anno.data", ChunkId.INCL, page, 68592L);
        Chunk mask = createChunk(6L, ChunkId.Sjbz, null, page, 68716L);
        Chunk bg1 = createChunk(7L, ChunkId.BG44, null, page, 68786L);
        Chunk bg2 = createChunk(8L, ChunkId.BG44, null, page, 68896L);
        Chunk bg3 = createChunk(9L, ChunkId.BG44, null, page, 68996L);
        Chunk fg = createChunk(10L, ChunkId.FG44, null, page, 69296L);
        Chunk text = createChunk(11L, ChunkId.TXTz, null, page, 69526L);

        List<Chunk> chunks = List.of(root,  dir, page, info, incl, mask,  bg1, bg2, bg3, fg, text);
        DjVuFile file = new DjVuFileImpl(chunks);

        Map<ChunkId, List<Chunk>> pageChunks = file.getAllPageChunks(info);

        assertEquals(Map.of(ChunkId.INFO, List.of(info),
                            ChunkId.INCL, List.of(incl),
                            ChunkId.Sjbz, List.of(mask),
                            ChunkId.BG44, List.of(bg1, bg2, bg3),
                            ChunkId.FG44, List.of(fg),
                            ChunkId.TXTz, List.of(text)),
                     pageChunks);
    }

    @Test
    public void testGetAllPageChunksWithSameChunkId() {
        Chunk root = createChunk(1L, ChunkId.FORM, SecondaryChunkId.DJVM, 12L);
        Chunk dir = readChunk(2L, "DIRM_with_shared_annotation.data", ChunkId.DIRM, root, 24L);

        Chunk page = createChunk(3L, ChunkId.FORM, SecondaryChunkId.DJVU, root, 68522L);
        Chunk info = createChunk(4L, ChunkId.INFO, null, page, 68576L);
        Chunk incl = readChunk(5L, "INCL_shared_anno.data", ChunkId.INCL, page, 68592L);
        Chunk mask = createChunk(6L, ChunkId.Sjbz, null, page, 68716L);
        Chunk bg1 = createChunk(7L, ChunkId.BG44, null, page, 68786L);
        Chunk bg2 = createChunk(8L, ChunkId.BG44, null, page, 68896L);
        Chunk bg3 = createChunk(9L, ChunkId.BG44, null, page, 68996L);
        Chunk fg = createChunk(10L, ChunkId.FG44, null, page, 69296L);
        Chunk text = createChunk(11L, ChunkId.TXTz, null, page, 69526L);

        List<Chunk> chunks = List.of(root,  dir, page, info, incl, mask,  bg1, bg2, bg3, fg, text);
        DjVuFile file = new DjVuFileImpl(chunks);

        List<Chunk> bgChunks = file.getAllPageChunksWithSameChunkId(bg2);

        assertEquals(List.of(bg1, bg2, bg3), bgChunks);
    }

    private Chunk createChunk(Long id, ChunkId chunkId, SecondaryChunkId secondaryId, Long offsetStart) {
        return createChunk(id, chunkId, secondaryId, null, offsetStart);
    }

    private Chunk createChunk(Long id, ChunkId chunkId, SecondaryChunkId secondaryId, Chunk chunk, Long offsetStart) {
        return Chunk.builder()
                .withId(id)
                .withChunkId(chunkId)
                .withSecondaryChunkId(secondaryId)
                .withParent(chunk)
                .withSize(10L)
                .withOffsetStart(offsetStart)
                .build();
    }
}
