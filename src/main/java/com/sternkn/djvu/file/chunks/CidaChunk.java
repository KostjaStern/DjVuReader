package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.DjVuFileException;
import com.sternkn.djvu.file.DjVuFileReader;
import com.sternkn.djvu.file.SimpleDataLogger;

/*
    We think that the unrecognized chunk "CIDa" is created by the Virtual Print Driver.
    This unrecognized chunk was not present in previous versions of the DjVu file format.
    It contains the string "msepdjvu3.6.1" followed by some additional binary information.
    The exact purpose of the additional binary data and of the purpose of this chunk in general is unknown to us.
 */
public class CidaChunk extends Chunk {

    private final byte[] data;
    private final String dataAsString;

    public CidaChunk(ChunkId chunkId, DjVuFileReader fileReader) {
        super(chunkId, fileReader.readChunkLength());

        this.data = new byte[this.getLength()];

        int numberOfBytesRead = fileReader.readBytes(this.data);

        if (numberOfBytesRead < this.data.length) {
            throw new DjVuFileException("Unexpected end of " + this.getChunkId() + " chunk after reading " +
                    numberOfBytesRead + " bytes");
        }

        SimpleDataLogger.logData(data, 10, "CIDa chunk data");
        this.dataAsString = new String(this.data);

        System.out.println("dataAsString = " + dataAsString);
    }

    @Override
    public String toString() {
        return "CidaChunk{chunkId = " + this.getChunkId()
                + ", length = " + this.getLength() + "}";
    }
}
