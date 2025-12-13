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
import com.sternkn.djvu.file.chunks.DirectoryChunk;

import java.util.List;
import java.util.Map;

public interface DjVuFile {

    /**
     * Returns all chunks in the DjVu file.
     *
     * @return a list of all chunks in the DjVu file
     */
    List<Chunk> getChunks();

    /**
     * Returns the DjVu file directory chunk (DIRM).
     *
     * @return the DIRM (directory) chunk of the DjVu file
     */
    DirectoryChunk getDirectoryChunk();

    /**
     * Returns the chunk with the given ID.
     *
     * @param chunkId the chunk ID
     * @return the chunk with the given ID
     */
    Chunk getChunkById(long chunkId);

    /**
     * Returns the chunk with the given offset.
     *
     * @param offset the chunk offset
     * @return the chunk with the given offset
     */
    Chunk getChunkByOffset(long offset);

    /**
     * Returns a map of chunk lists for the page that contains the given chunk, grouped by chunk ID.
     *
     * @param chunk a chunk from the {@code FORM:DJVU} container used to identify the page
     * @return a map where each key is a chunk ID and each value is the list of chunks with that ID on the page
     */
    Map<ChunkId, List<Chunk>> getAllPageChunks(Chunk chunk);

    /**
     * Returns the list of chunks on the page that contains the given chunk and that share its chunk ID.
     *
     * @param chunk the reference chunk
     * @return a list of chunks on the same page with the same parent and chunk ID as the given chunk
     */
    List<Chunk> getAllPageChunksWithSameChunkId(Chunk chunk);

    /**
     * Returns the shared-shape chunk ({@code Djbz}) for the given bitonal-mask chunk ({@code Sjbz}).
     *
     * @param chunk the bitonal-mask chunk ({@code Sjbz})
     * @return the shared-shape chunk ({@code Djbz})
     * @throws IllegalArgumentException if the given chunk is not an {@code Sjbz} chunk
     */
    Chunk findSharedShapeChunk(Chunk chunk);
}
