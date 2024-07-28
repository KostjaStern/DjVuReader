package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.DjVuFileReader;

/*
   The FG44, BG44, TH44 chunks use the same IW44 data store format
 */
public class Iw44Chunk extends Chunk {

    /*
       A one-octet unsigned integer. The serial number of the first chunk of a given chunk type is 0.
       Successive chunks are assigned consecutive serial numbers.
     */
    private final byte serialNumber;

    /*
        A one-octet unsigned integer. The number of slices coded in the chunk.
     */
    private final byte slicesNumber;



    public Iw44Chunk(ChunkId chunkId, DjVuFileReader fileReader) {
        super(chunkId, fileReader.readChunkLength());

        this.serialNumber = fileReader.readByte();
        this.slicesNumber = fileReader.readByte();

    }

    public byte getSerialNumber() {
        return this.serialNumber;
    }

    public byte getSlicesNumber() {
        return this.slicesNumber;
    }

    @Override
    public String toString() {
        return "Iw44Chunk{chunkId = " + this.getChunkId()
                + ", length = " + this.getLength()
                + ", serialNumber = " +  serialNumber
                + ", slicesNumber = " + slicesNumber
                + "}";
    }
}
