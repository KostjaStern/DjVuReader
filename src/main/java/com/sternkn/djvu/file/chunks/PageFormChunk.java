package com.sternkn.djvu.file.chunks;


import com.sternkn.djvu.file.DjVuFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
    FORM:DJVU
    A DjVu Page / single page DjVu document. Composite chunk that contains the chunks
    which make up a page in a djvu document.

    The nested first chunk must be the INFO chunk.
    The chunks after the INFO chunk may occur in any order, although the order of the BG44 chunks,
    if there is more than one, is significant.
 */
public class PageFormChunk extends FormChunk {
    private static final Logger LOG = LoggerFactory.getLogger(PageFormChunk.class);

    private final InfoChunk infoChunk;
    // private final byte[] data;

    public PageFormChunk(ChunkId chunkId, DjVuFileReader fileReader) {
        super(chunkId, fileReader);

        ChunkId infoChunkId = fileReader.readChunkId();
        this.infoChunk = new InfoChunk(infoChunkId, fileReader);

        ChunkId cidaChunkId = fileReader.readChunkId();
        CidaChunk cidaChunk = new CidaChunk(cidaChunkId, fileReader);
        LOG.debug("cidaChunk = {}", cidaChunk);

        /*
        this.data = new byte[this.getLength() - 9 - 4 - 4 - 4];

        int numberOfBytesRead = fileReader.readBytes(this.data);

        if (numberOfBytesRead < this.data.length) {
            throw new DjVuFileException("Unexpected end of " + this.getChunkId() + " chunk after reading " +
                    numberOfBytesRead + " bytes");
        }
        */

        ChunkId chunkId1 = fileReader.readChunkId();
        LOG.debug("chunkId1 = {}", chunkId1);

        // Iw44FirstChunk bg44 = new Iw44FirstChunk(chunkId1, fileReader);
        Iw44FirstChunk bg44_1 = new Iw44FirstChunk(chunkId1, fileReader, 1); // dataSize = 270913, data[270912] = 0
        LOG.debug("bg44_1 = {}", bg44_1);

        ChunkId chunkId2 = fileReader.readChunkId();
        LOG.debug("chunkId2 = {}", chunkId2);

        Bg44Chunk bg44_2 = new Bg44Chunk(chunkId2, fileReader, 0); // dataSize = 214740, data[214739] = -65
        LOG.debug("bg44_2 = {}", bg44_2);

        ChunkId chunkId3 = fileReader.readChunkId();
        LOG.debug("chunkId3 = {}", chunkId3);

        Bg44Chunk bg44_3 = new Bg44Chunk(chunkId3, fileReader, 1); // dataSize = 299300, data[299299] = 0
        LOG.debug("bg44_3 = {}", bg44_3);

        ChunkId chunkId4 = fileReader.readChunkId();
        LOG.debug("chunkId4 = {}", chunkId4);

        Bg44Chunk bg44_4 = new Bg44Chunk(chunkId4, fileReader, 1); // dataSize = 622654 , data[622653] = 0
        LOG.debug("bg44_4 = {}", bg44_4);
    }

    public InfoChunk getInfoChunk() {
        return this.infoChunk;
    }

    @Override
    public String toString() {
        return "PageFormChunk{chunkId = " + this.getChunkId()
                + ", length = " + this.getLength()
                + ", secondaryChunkId = " + this.getSecondaryChunkId() + "}";
    }
}
