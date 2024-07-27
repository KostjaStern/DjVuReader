package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.DjVuFileReader;

/*
    FORM:DJVU
    A DjVu Page / single page DjVu document. Composite chunk that contains the chunks
    which make up a page in a djvu document.

    The nested first chunk must be the INFO chunk.
    The chunks after the INFO chunk may occur in any order, although the order of the BG44 chunks,
    if there is more than one, is significant.
 */
public class PageFormChunk extends FormChunk {

    private final InfoChunk infoChunk;

    public PageFormChunk(ChunkId chunkId, DjVuFileReader fileReader) {
        super(chunkId, fileReader);

        ChunkId infoChunkId = fileReader.readChunkId();
        this.infoChunk = new InfoChunk(infoChunkId, fileReader);
    }

    public InfoChunk getInfoChunk() {
        return this.infoChunk;
    }

    @Override
    public String toString() {
        return "SharedFormChunk{chunkId = " + this.getChunkId()
                + ", length = " + this.getLength()
                + ", secondaryChunkId = " + this.getSecondaryChunkId() + "}";
    }
}
