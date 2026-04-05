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
package com.sternkn.djvu.benchmark.stubs;

import com.sternkn.djvu.file.DjVuFile;
import com.sternkn.djvu.file.chunks.Chunk;
import com.sternkn.djvu.file.chunks.ChunkId;
import com.sternkn.djvu.file.chunks.DirectoryChunk;
import com.sternkn.djvu.file.chunks.NavmChunk;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DjVuFileStub implements DjVuFile {

    private static final String PATH_CHUNKS = "benchmark_data/load/";
    private final ClassLoader classLoader = getClass().getClassLoader();

    @Override
    public List<Chunk> getChunks() {
        return List.of();
    }

    @Override
    public DirectoryChunk getDirectoryChunk() {
        return null;
    }

    @Override
    public Optional<NavmChunk> getNavigationMenu() {
        return Optional.empty();
    }

    @Override
    public Chunk getChunkById(long chunkId) {
        return null;
    }

    @Override
    public Chunk getChunkByOffset(long offset) {

        return switch((int) offset) {
            case 13192 -> readChunk(6L, "page1/INFO_6.data", ChunkId.INFO);
            case 315744 -> readChunk(60L, "page8/INFO_60.data", ChunkId.INFO);
            case 374690 -> readChunk(70L, "page9/INFO_70.data", ChunkId.INFO);
            case 427552 -> readChunk(80L, "page10/INFO_80.data", ChunkId.INFO);
            default -> null;
        };
    }

    @Override
    public Map<ChunkId, List<Chunk>> getAllPageChunks(Chunk chunk) {
        int chunkId = (int) chunk.getId();

        return switch(chunkId) {
            case 6 -> Map.of(
                ChunkId.INCL, List.of(readChunk(7L, "page1/INCL_7.data", ChunkId.INCL)),
                ChunkId.Sjbz, List.of(readChunk(8L, "page1/Sjbz_8.data", ChunkId.Sjbz)),
                ChunkId.FG44, List.of(readChunk(9L, "page1/FG44_9.data", ChunkId.FG44)),
                ChunkId.BG44, List.of(readChunk(10L, "page1/BG44_10.data", ChunkId.BG44),
                    readChunk(11L, "page1/BG44_11.data", ChunkId.BG44),
                    readChunk(12L, "page1/BG44_12.data", ChunkId.BG44),
                    readChunk(13L, "page1/BG44_13.data", ChunkId.BG44)),
                ChunkId.TXTz, List.of(readChunk(14L, "page1/TXTz_14.data", ChunkId.TXTz))
            );
            case 60 -> Map.of(
                    ChunkId.INCL, List.of(readChunk(61L, "page8/INCL_61.data", ChunkId.INCL)),
                    ChunkId.Sjbz, List.of(readChunk(62L, "page8/Sjbz_62.data", ChunkId.Sjbz)),
                    ChunkId.FG44, List.of(readChunk(63L, "page8/FG44_63.data", ChunkId.FG44)),
                    ChunkId.BG44, List.of(readChunk(64L, "page8/BG44_64.data", ChunkId.BG44),
                            readChunk(65L, "page8/BG44_65.data", ChunkId.BG44),
                            readChunk(66L, "page8/BG44_66.data", ChunkId.BG44),
                            readChunk(67L, "page8/BG44_67.data", ChunkId.BG44)),
                    ChunkId.TXTz, List.of(readChunk(68L, "page8/TXTz_68.data", ChunkId.TXTz))
            );
            case 70 -> Map.of(
                    ChunkId.INCL, List.of(readChunk(71L, "page9/INCL_71.data", ChunkId.INCL)),
                    ChunkId.Sjbz, List.of(readChunk(72L, "page9/Sjbz_72.data", ChunkId.Sjbz)),
                    ChunkId.FG44, List.of(readChunk(73L, "page9/FG44_73.data", ChunkId.FG44)),
                    ChunkId.BG44, List.of(readChunk(74L, "page9/BG44_74.data", ChunkId.BG44),
                            readChunk(75L, "page9/BG44_75.data", ChunkId.BG44),
                            readChunk(76L, "page9/BG44_76.data", ChunkId.BG44),
                            readChunk(77L, "page9/BG44_77.data", ChunkId.BG44)),
                    ChunkId.TXTz, List.of(readChunk(78L, "page9/TXTz_78.data", ChunkId.TXTz))
            );
            case 80 -> Map.of(
                    ChunkId.INCL, List.of(readChunk(81L, "page10/INCL_81.data", ChunkId.INCL)),
                    ChunkId.Sjbz, List.of(readChunk(82L, "page10/Sjbz_82.data", ChunkId.Sjbz)),
                    ChunkId.FG44, List.of(readChunk(83L, "page10/FG44_83.data", ChunkId.FG44)),
                    ChunkId.BG44, List.of(readChunk(84L, "page10/BG44_84.data", ChunkId.BG44),
                            readChunk(85L, "page10/BG44_85.data", ChunkId.BG44),
                            readChunk(86L, "page10/BG44_86.data", ChunkId.BG44),
                            readChunk(87L, "page10/BG44_87.data", ChunkId.BG44)),
                    ChunkId.TXTz, List.of(readChunk(88L, "page10/TXTz_88.data", ChunkId.TXTz))
            );
            default -> null;
        };
    }

    @Override
    public List<Chunk> getAllPageChunksWithSameChunkId(Chunk chunk) {
        return List.of();
    }

    @Override
    public Chunk findSharedShapeChunk(Chunk chunk) {
        int chunkId = (int) chunk.getId();

        return switch(chunkId) {
            case 8, 62, 72, 82 -> readChunk(4L, "Djbz_4.data", ChunkId.Djbz);
            default -> null;
        };
    }

    private InputStream readStream(String fileName) {
        return classLoader.getResourceAsStream(PATH_CHUNKS + fileName);
    }

    private byte[] readByteBuffer(String fileName) {
        try (InputStream inputStream = readStream(fileName)) {
            return inputStream != null ? inputStream.readAllBytes() : new byte[0];
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Chunk readChunk(Long id, String fileName, ChunkId chunkId) {
        byte[] buffer = readByteBuffer(fileName);

        return Chunk.builder()
                .withId(id)
                .withChunkId(chunkId)
                .withData(buffer)
                .withSize(buffer.length).build();
    }
}
