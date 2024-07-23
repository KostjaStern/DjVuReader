package com.sternkn.djvu.file.chunks;

//import com.sternkn.djvu.file.DjVuFileException;
//import java.io.DataInputStream;
//import java.io.IOException;


public abstract class Chunk {

    private final ChunkId chunkId;
    private final int length;

    // protected final DataInputStream inputStream;

    public Chunk(ChunkId chunkId, int length) {
        this.chunkId = chunkId;
        this.length = length;
        // this.inputStream = stream;

        // this.length = readChunkLength();
        // this.chunkId = readChunkId();
    }

    public ChunkId getChunkId() {
        return this.chunkId;
    }

    public int getLength() {
        return this.length;
    }
}
