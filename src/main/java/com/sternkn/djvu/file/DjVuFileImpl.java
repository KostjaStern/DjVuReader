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

    private final MagicHeader header;
    private final List<Chunk> chunks;
    private final long fileSize;
    private final DirectoryChunk directoryChunk;

    public DjVuFileImpl() {
        this(MagicHeader.AT_T, List.of(), 0L);
    }

    public DjVuFileImpl(MagicHeader header, List<Chunk> chunks, long fileSize) {
        this.header = header;
        this.chunks = chunks;
        this.fileSize = fileSize;
        this.directoryChunk = findDirectory();
    }

    @Override
    public MagicHeader getHeader() {
        return header;
    }

    @Override
    public List<Chunk> getChunks() {
        return chunks;
    }

    @Override
    public long getFileSize() {
        return fileSize;
    }

    @Override
    public DirectoryChunk getDirectoryChunk() {
        return directoryChunk;
    }

    private DirectoryChunk findDirectory() {
        List<Chunk> cks = this.chunks.stream()
                .filter(c -> c.getChunkId() == ChunkId.DIRM).toList();

        if (cks.isEmpty()) {
            throw new DjVuFileException("The DIRM chunk is missing from the DjVu file.");
        }

        if (cks.size() > 1) {
            LOG.warn("More than one DIRM chunk ({}) were found", cks.size());
        }

        Chunk chunk = cks.getFirst();
        return new DirectoryChunk(chunk);
    }

    @Override
    public Chunk getChunkById(long chunkId) {
        List<Chunk> chunks = this.chunks.stream()
                .filter(c -> c.getId() == chunkId).toList();

        if (chunks.isEmpty()) {
            throw new DjVuFileException("Chunk with id " + chunkId + " not found");
        }

        if (chunks.size() > 1) {
            LOG.warn("More than one chunk with id {} were found", chunkId);
        }

        return chunks.getFirst();
    }

    @Override
    public Chunk getChunkByOffset(long offset) {
        final long chunkOffset = OFFSET_ALIGNMENT + offset;
        List<Chunk> cks = this.chunks.stream()
                .filter(c -> chunkOffset == c.getOffsetStart()).toList();

        if (cks.isEmpty()) {
            throw new DjVuFileException("Chunk with offset " + offset + " not found");
        }

        if (cks.size() > 1) {
            LOG.warn("More than one chunk with offset {} were found", offset);
        }

        return cks.getFirst();
    }

    @Override
    public Map<ChunkId, List<Chunk>> getAllPageChunks(Chunk chunk) {
        Chunk parent = chunk.getParent();
        return this.chunks.stream()
            .filter(c -> c.getParent() != null && c.getParent().getId() == parent.getId())
            .filter(c -> c.getChunkId() != ChunkId.INFO)
            .collect(Collectors.groupingBy(Chunk::getChunkId));
    }

    @Override
    public List<Chunk> getAllImageChunks(Chunk chunk) {
        Chunk parent = chunk.getParent();
        return this.chunks.stream()
            .filter(c -> c.getParent() != null && c.getParent().getId() == parent.getId())
            .filter(c -> c.getChunkId() == chunk.getChunkId())
            .toList();
    }

    /**
     *
     * @param chunk - Sjbz chunk
     * @return related Djbz chunk
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
