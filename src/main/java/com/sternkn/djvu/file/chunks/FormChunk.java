package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.DjVuFileReader;

public abstract class FormChunk extends Chunk {

    private final SecondaryChunkId secondaryChunkId;

    public FormChunk(ChunkId chunkId, DjVuFileReader fileReader) {
        super(chunkId, fileReader.readChunkLength());

        this.secondaryChunkId = fileReader.readSecondaryChunkId();
    }

    public SecondaryChunkId getSecondaryChunkId() {
        return this.secondaryChunkId;
    }
}
