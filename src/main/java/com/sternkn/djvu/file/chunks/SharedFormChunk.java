package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.DjVuFileReader;

/*
   FORM:DJVI
   A “shared” DjVu file which is included via the INCL chunk. Shared annotations, shared shape dictionary.
 */
public class SharedFormChunk extends FormChunk {

    private final DjbzChunk djbzChunk;

    public SharedFormChunk(ChunkId chunkId, DjVuFileReader fileReader) {
        super(chunkId, fileReader);

        ChunkId djbzChunkId = fileReader.readChunkId();
        this.djbzChunk = new DjbzChunk(djbzChunkId, fileReader);
    }

    public DjbzChunk getDjbzChunk() {
        return this.djbzChunk;
    }

    @Override
    public String toString() {
        return "SharedFormChunk{chunkId = " + this.getChunkId()
                + ", length = " + this.getLength()
                + ", secondaryChunkId = " + this.getSecondaryChunkId()
                + ", djbzChunk = " + this.djbzChunk + "}";
    }
}
