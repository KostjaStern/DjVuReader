package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.DjVuFileException;
import com.sternkn.djvu.file.DjVuFileReader;
import com.sternkn.djvu.file.SimpleDataLogger;

/*
   Shared shape table

   11.4.10.1 Shared Shape Dictionaries
 */
public class DjbzChunk extends Chunk {

    private final byte[] jb2;

    public DjbzChunk(ChunkId chunkId, DjVuFileReader fileReader) {
        super(chunkId, fileReader.readChunkLength());

        this.jb2 = new byte[this.getLength() + 1];

        int numberOfBytesRead = fileReader.readBytes(this.jb2);

        if (numberOfBytesRead < this.jb2.length) {
            throw new DjVuFileException("Unexpected end of " + this.getChunkId() + " chunk after reading " +
                    numberOfBytesRead + " bytes");
        }

        SimpleDataLogger.logData(jb2, 10, "jb2");
    }

    @Override
    public String toString() {
        return "DjbzChunk{chunkId = " + this.getChunkId()
                + ", length = " + this.getLength() + "}";
    }
}
