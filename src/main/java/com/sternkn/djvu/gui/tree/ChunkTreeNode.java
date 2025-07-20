package com.sternkn.djvu.gui.tree;

import com.sternkn.djvu.file.chunks.Chunk;

public class ChunkTreeNode {

    private final Chunk chunk;

    public ChunkTreeNode(Chunk chunk) {
        this.chunk = chunk;
    }

    public Chunk getChunk() {
        return chunk;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ChunkTreeNode other)) {
            return false;
        }
        return chunk.getId() == other.chunk.getId();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(chunk.getId());
    }

    @Override
    public String toString() {
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
}
