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
import java.util.Set;
import java.util.stream.Collectors;


public class DjVuFile {
    private static final Logger LOG = LoggerFactory.getLogger(DjVuFile.class);

    private static final int OFFSET_ALIGNMENT = 20;

    private final MagicHeader header;
    private final List<Chunk> chunks;
    private final long fileSize;
    private final DirectoryChunk directoryChunk;

    public DjVuFile() {
        this(MagicHeader.AT_T, List.of(), 0L);
    }

    public DjVuFile(MagicHeader header, List<Chunk> chunks, long fileSize) {
        this.header = header;
        this.chunks = chunks;
        this.fileSize = fileSize;
        this.directoryChunk = findDirectory(chunks);
    }

    public MagicHeader getHeader() {
        return header;
    }

    public List<Chunk> getChunks() {
        return chunks;
    }

    public long getFileSize() {
        return fileSize;
    }

    public DirectoryChunk getDirectoryChunk() {
        return directoryChunk;
    }

    private DirectoryChunk findDirectory(List<Chunk> chunks) {
        return chunks.stream().filter(chunk -> chunk.getChunkId() == ChunkId.DIRM)
                .findFirst()
                .map(DirectoryChunk::new)
                .orElse(null);
    }

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
