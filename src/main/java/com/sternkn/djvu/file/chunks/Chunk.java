package com.sternkn.djvu.file.chunks;


import com.sternkn.djvu.file.DjVuFileException;

import java.io.ByteArrayInputStream;

import static com.sternkn.djvu.file.utils.StringUtils.NL;

public class Chunk {

    private final long id;
    private final ChunkId chunkId;
    private final SecondaryChunkId secondaryChunkId;

    private final Chunk parent;
    protected final byte[] data;

    /**
     * This is true only for ChunkId.FORM
     */
    private final boolean isComposite;

    /**
     *  This is an offset of the first data byte (not including chunkId and secondaryChunkId if it exists)
     **/
    private final long offsetStart;

    /**
     * This is an offset of the last data byte
     */
    private final long offsetEnd;

    /**
     *  Actually, size = offsetEnd - offsetStart
     */
    private final long size;

    public Chunk(Chunk chunk) {
        this.id = chunk.getId();
        this.chunkId = chunk.getChunkId();
        this.secondaryChunkId = chunk.getSecondaryChunkId();
        this.parent = chunk.getParent();
        this.data = chunk.getData();
        this.isComposite = chunk.isComposite();
        this.offsetStart = chunk.getOffsetStart();
        this.offsetEnd = chunk.getOffsetEnd();
        this.size = chunk.getSize();
    }

    public Chunk(Builder builder) {
        this.id = builder.id;
        this.chunkId = builder.chunkId;
        this.secondaryChunkId = builder.secondaryChunkId;
        this.parent = builder.parent;
        this.isComposite = this.chunkId.isComposite();
        this.offsetStart = builder.offsetStart;
        this.size = builder.size;
        this.offsetEnd = this.offsetStart + this.size;
        this.data = builder.data;

        if (this.size <= 0) {
            throw new DjVuFileException(String.format("The chunk %s:%s size is %s - negative value",
                    this.chunkId, this.secondaryChunkId, this.size));
        }
    }

    public long getId() {
        return this.id;
    }

    public ChunkId getChunkId() {
        return this.chunkId;
    }

    public SecondaryChunkId getSecondaryChunkId() {
        return this.secondaryChunkId;
    }

    public String getCompositeChunkId() {
        if (this.isComposite) {
            return String.format("%s:%s", this.chunkId.name(), this.secondaryChunkId.name());
        }
        else {
            return this.chunkId.name();
        }
    }

    public Chunk getParent() {
        return this.parent;
    }

    public boolean isComposite() {
        return this.isComposite;
    }

    public long getOffsetStart() {
        return this.offsetStart;
    }

    public long getOffsetEnd() {
        return this.offsetEnd;
    }

    public long getSize() {
        return this.size;
    }

    public byte[] getData() {
        return this.data;
    }

    public String getDataAsText() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(" ChunkId: ").append(chunkId).append(NL);
        if (isComposite()) {
            buffer.append(" SecondaryChunkId: ").append(secondaryChunkId).append(NL);
        }
        buffer.append(" OffsetStart: ").append(offsetStart).append(NL);
        buffer.append(" OffsetEnd: ").append(offsetEnd).append(NL);
        buffer.append(" Size: ").append(size).append(NL);

        return buffer.toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long id;
        private ChunkId chunkId;
        private SecondaryChunkId secondaryChunkId;
        private Chunk parent;
        private long offsetStart;
        private long size;
        private byte[] data;

        public Builder() {
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withChunkId(ChunkId chunkId) {
            this.chunkId = chunkId;
            return this;
        }

        public Builder withSecondaryChunkId(SecondaryChunkId secondaryChunkId) {
            this.secondaryChunkId = secondaryChunkId;
            return this;
        }

        public Builder withParent(Chunk parent) {
            this.parent = parent;
            return this;
        }

        public Builder withOffsetStart(long offsetStart) {
            this.offsetStart = offsetStart;
            return this;
        }

        public Builder withSize(long size) {
            this.size = size;
            return this;
        }

        public Builder withData(byte[] data) {
            this.data = data;
            return this;
        }

        public Chunk build() {
            return new Chunk(this);
        }
    }

    @Override
    public String toString() {
        final Long parentId = parent == null ? null : parent.id;
        return String.format("Chunk{id = %s, chunkId = %s, secondaryChunkId = %s, parentId = %s, " +
            "isComposite = %s, offsetStart = %s, offsetEnd = %s, size = %s }",
            id, chunkId, secondaryChunkId, parentId, isComposite, offsetStart, offsetEnd, size);
    }
}
