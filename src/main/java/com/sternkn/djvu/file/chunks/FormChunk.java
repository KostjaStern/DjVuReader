package com.sternkn.djvu.file.chunks;


public abstract class FormChunk {

    private SecondaryChunkId secondaryChunkId;

    public FormChunk() {
    }

    public SecondaryChunkId getSecondaryChunkId() {
        return this.secondaryChunkId;
    }
}
