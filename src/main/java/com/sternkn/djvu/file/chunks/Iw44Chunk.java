package com.sternkn.djvu.file.chunks;


/*
   The FG44, BG44, TH44 chunks use the same IW44 data store format
 */
public class Iw44Chunk {

    /*
       A one-octet unsigned integer. The serial number of the first chunk of a given chunk type is 0.
       Successive chunks are assigned consecutive serial numbers.
     */
    private byte serialNumber;

    /*
        A one-octet unsigned integer. The number of slices coded in the chunk.
     */
    private byte slicesNumber;

    public Iw44Chunk() {
//        super(chunkId, fileReader.readChunkLength());
//
//        this.serialNumber = fileReader.readByte();
//        this.slicesNumber = fileReader.readByte();
    }

    public byte getSerialNumber() {
        return this.serialNumber;
    }

    public byte getSlicesNumber() {
        return this.slicesNumber;
    }

//    @Override
//    public String toString() {
//        return "Iw44Chunk{chunkId = " + this.getChunkId()
//                + ", length = " + this.getLength()
//                + ", serialNumber = " +  serialNumber
//                + ", slicesNumber = " + slicesNumber
//                + "}";
//    }
}
