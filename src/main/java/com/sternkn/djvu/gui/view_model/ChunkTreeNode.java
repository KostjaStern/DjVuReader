package com.sternkn.djvu.gui.view_model;

import com.sternkn.djvu.file.chunks.Chunk;

public class ChunkTreeNode {

    private final long chunkId;
    private final String chunkName;
    private final String nodeName;

    public ChunkTreeNode(Chunk chunk) {
        this.chunkId = chunk.getId();
        this.chunkName = chunk.getChunkId().name();
        this.nodeName = buildNodeName(chunk);
    }

    private String buildNodeName(Chunk chunk) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(chunk.getChunkId().name());
        if (chunk.isComposite()) {
            buffer.append(":");
            buffer.append(chunk.getSecondaryChunkId().name());
        }
        buffer.append(" [");
        buffer.append(chunk.getSize());
        buffer.append("]");

        return buffer.toString();
    }

    public long getChunkId() {
        return chunkId;
    }

    public String getChunkName() {
        return chunkName;
    }

    public String getNodeName() {
        return nodeName;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ChunkTreeNode other)) {
            return false;
        }
        return chunkId == other.chunkId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(chunkId);
    }

    @Override
    public String toString() {
        return nodeName;
    }
}
