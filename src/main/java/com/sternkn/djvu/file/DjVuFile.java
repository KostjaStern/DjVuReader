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
import java.util.Objects;


public class DjVuFile {
    private static final Logger LOG = LoggerFactory.getLogger(DjVuFile.class);

    private static final int OFFSET_ALIGNMENT = 20;

    private final MagicHeader header;
    private final List<Chunk> chunks;
    private final long fileSize;
    private final DirectoryChunk directoryChunk;

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

    /**
     *
     * @param chunk - Sjbz chunk
     * @return related Djbz chunk
     */
    public Chunk findSharedShapeChunk(Chunk chunk) {

        if (chunk.getChunkId() != ChunkId.Sjbz) {
            throw new IllegalArgumentException("Chunk id " + chunk.getChunkId() + " is not a JB2 bitonal data chunk");
        }

        final String sharedComponentID = getSharedComponentID(chunk);
        if (sharedComponentID == null) {
            LOG.debug("No shared component ID found for chunk - {}", chunk);
            return null;
        }

        Long offset = directoryChunk.getComponents().stream()
            .filter(c -> Objects.equals(c.getId(),  sharedComponentID))
            .map(ComponentInfo::getOffset).findFirst().orElse(null);

        ComponentInfo component = directoryChunk.getComponents().stream()
                .filter(c -> Objects.equals(c.getId(),  sharedComponentID))
                .findFirst().orElse(null);

        if (component == null) {
            LOG.warn("We can not find component for shared component ID: {}", sharedComponentID);
            return null;
        }

        if (component.getType() == ComponentType.SHARED_ANNO) {
            LOG.debug("The shared component ID - {} has type {}", sharedComponentID,  component.getType());
            return null;
        }

        if (offset == null) {
            throw new DjVuFileException("We can not find directory record for shared component ID " + sharedComponentID);
        }

        Long chunkOffset = offset + OFFSET_ALIGNMENT;

        final Chunk sharedShapeChunk = this.chunks.stream()
                .filter(c -> Objects.equals(c.getOffsetStart(),  chunkOffset)
                                    && c.getChunkId() == ChunkId.Djbz)
                .findFirst().orElse(null);

        if (sharedShapeChunk == null) {
            LOG.debug("No shared shape chunk found for chunk - {}", chunk);
        }

        return sharedShapeChunk;
    }

    private String getSharedComponentID(Chunk chunk) {
        Chunk parent = chunk.getParent();
        return this.chunks.stream()
                .filter(c -> c.getParent() != null && c.getParent().getId() == parent.getId())
                .filter(c -> c.getChunkId() == ChunkId.INCL)
                .findFirst()
                .map(InclChunk::new)
                .map(InclChunk::getSharedComponentID)
                .orElse(null);
    }
}
