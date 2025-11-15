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
package com.sternkn.djvu.gui.view_model;

import com.sternkn.djvu.file.chunks.Chunk;
import com.sternkn.djvu.file.chunks.ChunkId;

public class ChunkTreeNode {

    private final long chunkId;
    private final boolean isComposite;
    private final String chunkName;
    private final String nodeName;

    public ChunkTreeNode(Chunk chunk) {
        this.chunkId = chunk.getId();

        ChunkId chunkType = chunk.getChunkId();

        this.isComposite = chunkType.isComposite();
        this.chunkName = chunkType.name();
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

    public boolean isComposite() {
        return isComposite;
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
