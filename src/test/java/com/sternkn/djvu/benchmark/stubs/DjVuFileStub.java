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
            case 1 -> readChunk(1L, "page1/INFO_1.data", ChunkId.INFO);
            case 2 -> readChunk(10L, "page2/INFO_10.data", ChunkId.INFO);
            case 3 -> readChunk(20L, "page3/INFO_20.data", ChunkId.INFO);
            case 4 -> readChunk(30L, "page4/INFO_30.data", ChunkId.INFO);

            case 6 -> readChunk(50L, "page6/INFO_50.data", ChunkId.INFO);
            case 7 -> readChunk(60L, "page7/INFO_60.data", ChunkId.INFO);
            case 8 -> readChunk(70L, "page8/INFO_70.data", ChunkId.INFO);
            case 9 -> readChunk(80L, "page9/INFO_80.data", ChunkId.INFO);
            default -> null;
        };
    }

    @Override
    public Map<ChunkId, List<Chunk>> getAllPageChunks(Chunk chunk) {
        int chunkId = (int) chunk.getId();

        return switch(chunkId) {
            case 1 -> Map.of(
                ChunkId.INCL, List.of(readChunk(2L, "page1/INCL_2.data", ChunkId.INCL)),
                ChunkId.FG44, List.of(readChunk(3L, "page1/FG44_3.data", ChunkId.FG44)),
                ChunkId.Sjbz, List.of(readChunk(4L, "page1/Sjbz_4.data", ChunkId.Sjbz)),
                ChunkId.TXTz, List.of(readChunk(5L, "page1/TXTz_5.data", ChunkId.TXTz)),
                ChunkId.BG44, List.of(readChunk(6L, "page1/BG44_6.data", ChunkId.BG44),
                    readChunk(7L, "page1/BG44_7.data", ChunkId.BG44),
                    readChunk(8L, "page1/BG44_8.data", ChunkId.BG44),
                    readChunk(9L, "page1/BG44_9.data", ChunkId.BG44))
            );
            case 10 -> Map.of(
                ChunkId.INCL, List.of(readChunk(11L, "page2/INCL_11.data", ChunkId.INCL)),
                ChunkId.FG44, List.of(readChunk(12L, "page2/FG44_12.data", ChunkId.FG44)),
                ChunkId.Sjbz, List.of(readChunk(13L, "page2/Sjbz_13.data", ChunkId.Sjbz)),
                ChunkId.TXTz, List.of(readChunk(14L, "page2/TXTz_14.data", ChunkId.TXTz)),
                ChunkId.BG44, List.of(readChunk(15L, "page2/BG44_15.data", ChunkId.BG44),
                    readChunk(16L, "page2/BG44_16.data", ChunkId.BG44),
                    readChunk(17L, "page2/BG44_17.data", ChunkId.BG44),
                    readChunk(18L, "page2/BG44_18.data", ChunkId.BG44))
            );
            case 20 -> Map.of(
                ChunkId.INCL, List.of(readChunk(21L, "page3/INCL_21.data", ChunkId.INCL)),
                ChunkId.Sjbz, List.of(readChunk(22L, "page3/Sjbz_22.data", ChunkId.Sjbz)),
                ChunkId.FG44, List.of(readChunk(23L, "page3/FG44_23.data", ChunkId.FG44)),
                ChunkId.BG44, List.of(readChunk(24L, "page3/BG44_24.data", ChunkId.BG44),
                    readChunk(25L, "page3/BG44_25.data", ChunkId.BG44),
                    readChunk(26L, "page3/BG44_26.data", ChunkId.BG44),
                    readChunk(27L, "page3/BG44_27.data", ChunkId.BG44)),
                ChunkId.TXTz, List.of(readChunk(28L, "page3/TXTz_28.data", ChunkId.TXTz))
            );
            case 30 -> Map.of(
                ChunkId.INCL, List.of(readChunk(31L, "page4/INCL_31.data", ChunkId.INCL)),
                ChunkId.Sjbz, List.of(readChunk(32L, "page4/Sjbz_32.data", ChunkId.Sjbz)),
                ChunkId.FG44, List.of(readChunk(33L, "page4/FG44_33.data", ChunkId.FG44)),
                ChunkId.BG44, List.of(readChunk(34L, "page4/BG44_34.data", ChunkId.BG44),
                    readChunk(35L, "page4/BG44_35.data", ChunkId.BG44),
                    readChunk(36L, "page4/BG44_36.data", ChunkId.BG44),
                    readChunk(37L, "page4/BG44_37.data", ChunkId.BG44)),
                ChunkId.TXTz, List.of(readChunk(38L, "page4/TXTz_38.data", ChunkId.TXTz))
            );

            case 50 -> Map.of(
                ChunkId.INCL, List.of(readChunk(51L, "page6/INCL_51.data", ChunkId.INCL)),
                ChunkId.TXTz, List.of(readChunk(52L, "page6/TXTz_52.data", ChunkId.TXTz)),
                ChunkId.Sjbz, List.of(readChunk(53L, "page6/Sjbz_53.data", ChunkId.Sjbz))
            );
            case 60 -> Map.of(
                ChunkId.INCL, List.of(readChunk(61L, "page7/INCL_61.data", ChunkId.INCL)),
                ChunkId.Sjbz, List.of(readChunk(62L, "page7/Sjbz_62.data", ChunkId.Sjbz)),
                ChunkId.TXTz, List.of(readChunk(63L, "page7/TXTz_63.data", ChunkId.TXTz))
            );
            case 70 -> Map.of(
                ChunkId.INCL, List.of(readChunk(71L, "page8/INCL_71.data", ChunkId.INCL)),
                ChunkId.Sjbz, List.of(readChunk(72L, "page8/Sjbz_72.data", ChunkId.Sjbz)),
                ChunkId.TXTz, List.of(readChunk(73L, "page8/TXTz_73.data", ChunkId.TXTz))
            );
            case 80 -> Map.of(
                ChunkId.INCL, List.of(readChunk(81L, "page9/INCL_81.data", ChunkId.INCL)),
                ChunkId.Sjbz, List.of(readChunk(82L, "page9/Sjbz_82.data", ChunkId.Sjbz)),
                ChunkId.TXTz, List.of(readChunk(83L, "page9/TXTz_83.data", ChunkId.TXTz))
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
            case 4, 13, 22, 32 -> readChunk(41L, "Djbz_4.data", ChunkId.Djbz);
            case 53, 62, 72, 82 -> readChunk(42L, "Djbz_5.data", ChunkId.Djbz);
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
