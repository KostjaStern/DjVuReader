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
import com.sternkn.djvu.file.chunks.ComponentInfo;
import com.sternkn.djvu.file.chunks.ComponentType;
import com.sternkn.djvu.file.chunks.DirectoryChunk;
import com.sternkn.djvu.file.chunks.InclChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class DjVuFileImpl implements DjVuFile {
    private static final Logger LOG = LoggerFactory.getLogger(DjVuFileImpl.class);

    private static final int OFFSET_ALIGNMENT = 20;

    private final List<Chunk> chunks;
    private final DirectoryChunk directoryChunk;

    public DjVuFileImpl(List<Chunk> chunks) {
        this.chunks = chunks;
        this.directoryChunk = findDirectory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Chunk> getChunks() {
        return chunks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DirectoryChunk getDirectoryChunk() {
        return directoryChunk;
    }

    private DirectoryChunk findDirectory() {
        List<Chunk> cks = this.chunks.stream()
                .filter(c -> c.getChunkId() == ChunkId.DIRM).toList();

        final String emptyError = "The DjVu file is missing the DIRM chunk.";
        final String manyWarning = String.format("Multiple DIRM chunks were found (%d).", cks.size());

        Chunk chunk = listToOne(cks, emptyError, manyWarning);
        return new DirectoryChunk(chunk);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Chunk getChunkById(long chunkId) {
        List<Chunk> cks = this.chunks.stream()
                .filter(c -> c.getId() == chunkId).toList();

        final String emptyError = String.format("Chunk with id %d not found.", chunkId);
        final String manyWarning = String.format("More than one chunk with id %d were found.", chunkId);

        return listToOne(cks, emptyError, manyWarning);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Chunk getChunkByOffset(long offset) {
        final long chunkOffset = OFFSET_ALIGNMENT + offset;
        final List<Chunk> cks = this.chunks.stream()
                .filter(c -> chunkOffset == c.getOffsetStart()).toList();

        final String emptyError = String.format("Chunk with offset %d not found.", offset);
        final String manyWarning = String.format("More than one chunk with offset %d were found.", offset);

        return listToOne(cks, emptyError, manyWarning);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<ChunkId, List<Chunk>> getAllPageChunks(Chunk chunk) {
        Chunk parent = chunk.getParent();
        return this.chunks.stream()
            .filter(c -> c.getParent() != null && c.getParent().getId() == parent.getId())
            .collect(Collectors.groupingBy(Chunk::getChunkId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Chunk> getAllPageChunksWithSameChunkId(Chunk chunk) {
        Chunk parent = chunk.getParent();
        return this.chunks.stream()
            .filter(c -> c.getParent() != null && c.getParent().getId() == parent.getId())
            .filter(c -> c.getChunkId() == chunk.getChunkId())
            .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Chunk findSharedShapeChunk(Chunk chunk) {

        if (chunk.getChunkId() != ChunkId.Sjbz) {
            throw new IllegalArgumentException("Chunk id " + chunk.getChunkId() + " is not a JB2 bitonal data chunk");
        }

        final List<String> sharedComponentIDs = getSharedComponentIDs(chunk);
        if (sharedComponentIDs.isEmpty()) {
            LOG.debug("No shared component ID found for chunk - {}", chunk);
            return null;
        }

        List<ComponentInfo> components = directoryChunk.getComponents().stream()
            .filter(c -> sharedComponentIDs.contains(c.getId()))
            .filter(c -> c.getType() == ComponentType.INCLUDED)
            .toList();

        if (components.isEmpty()) {
            LOG.warn("We can not find component for shared component IDs: {}", sharedComponentIDs);
            return null;
        }

        Set<Long> offsets = components.stream()
            .map(c -> c.getOffset() + OFFSET_ALIGNMENT)
            .collect(Collectors.toSet());

        final Chunk sharedShapeChunk = this.chunks.stream()
            .filter(c -> c.getChunkId() == ChunkId.Djbz)
            .filter(c -> offsets.contains(c.getOffsetStart()))
            .findFirst().orElse(null);

        if (sharedShapeChunk == null) {
            LOG.debug("No shared shape chunk found for chunk - {}", chunk);
        }

        return sharedShapeChunk;
    }

    private Chunk listToOne(List<Chunk> list, String emptyError, String manyWarning) {
        if (list.isEmpty()) {
            throw new DjVuFileException(emptyError);
        }

        if (list.size() > 1) {
            LOG.warn(manyWarning);
        }

        return list.getFirst();
    }

    private List<String> getSharedComponentIDs(Chunk chunk) {
        Chunk parent = chunk.getParent();
        return this.chunks.stream()
                .filter(c -> c.getParent() != null && c.getParent().getId() == parent.getId())
                .filter(c -> c.getChunkId() == ChunkId.INCL)
                .map(InclChunk::new)
                .map(InclChunk::getSharedComponentID)
                .toList();
    }
}
