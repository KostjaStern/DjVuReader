package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.DjVuFileException;
import com.sternkn.djvu.file.DjVuFileReader;
import com.sternkn.djvu.file.SimpleDataLogger;

public class Bg44Chunk extends Chunk {

    private final byte serialNumber;
    private final byte slicesNumber;

    private final byte[] data;

    public Bg44Chunk(ChunkId chunkId, DjVuFileReader fileReader, int sizeAdjustment) {
        super(chunkId, fileReader.readChunkLength());

        this.serialNumber = fileReader.readByte();
        this.slicesNumber = fileReader.readByte();

        this.data = new byte[this.getLength() + sizeAdjustment - 2];
        int numberOfBytesRead = fileReader.readBytes(this.data);

        if (numberOfBytesRead < this.data.length) {
            throw new DjVuFileException("Unexpected end of " + this.getChunkId() + " chunk after reading " +
                    numberOfBytesRead + " bytes");
        }

        SimpleDataLogger.logData(data, 20, "bg44 data");
    }

//    public short getSerialNumber() {
//        return this.serialNumber;
//    }
//
//    public short getSlicesNumber() {
//        return this.slicesNumber;
//    }


    @Override
    public String toString() {
        return "Bg44Chunk{chunkId = " + this.getChunkId()
                + ", length = " + this.getLength()
                + ", serialNumber = " +  serialNumber
                + ", slicesNumber = " + slicesNumber
                + "}";
    }
}
