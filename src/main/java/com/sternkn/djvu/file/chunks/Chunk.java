package com.sternkn.djvu.file.chunks;


public abstract class Chunk {

    private final ChunkId chunkId;
    private final int length;

    public Chunk(ChunkId chunkId, int length) {
        this.chunkId = chunkId;
        this.length = length;
    }

    public ChunkId getChunkId() {
        return this.chunkId;
    }

    public int getLength() {
        return this.length;
    }
}
