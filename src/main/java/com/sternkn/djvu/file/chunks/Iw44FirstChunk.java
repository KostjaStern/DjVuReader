package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.DjVuFileException;
import com.sternkn.djvu.file.DjVuFileReader;
import com.sternkn.djvu.file.SimpleDataLogger;

public class Iw44FirstChunk extends Iw44Chunk {

    /*
        One octet containing two values, present only if the serial number is 0.
        The least significant seven bits designate the major version number of the standard being implemented
        by the decoder. For this version of the standard, the major version number is 1.

        The most significant bit is the color type bit. The color type bit is 0 if the chunk describes
        three color components. The color type bit is I if the chunk describes one color component.
    */
    private final byte majorVersionAndColorType;

    private final boolean isOneColorComponent;
    private final int majorVersion;

    /*
       A one-octet unsigned integer, present only if the serial umber is 0.
       This octet designates the minor version number of the standard being implemented by the decoder.
       For this version of the standard, the minor version number is 2.
     */
    private final byte minorVersion;

    /*
       A two-octet unsigned integer, most significant octet first, present only if the serial number is 0.
       This field indicates the number of pixels in each row of the image described by the current chunk.
     */
    private final short width;

    /*
       A two-octet unsigned integer, most significant octet first, present only if the serial number is 0.
       This field indicates the number of pixels in each column of the image described by the current chunk.
     */
    private final short height;

    /*
       Initial value of chrominance delay counter. A one-octet unsigned integer, present only if the serial number is 0.
       Only the least significant seven bits are used. The most significant bit is ignored,
       but should be set to 1 by an encoder.
     */
    private final byte delayCounter;

    private final int chrominanceDelayCounter;

    private final byte[] data;


    public Iw44FirstChunk(ChunkId chunkId, DjVuFileReader fileReader, int sizeAdjustment) {
        super(chunkId, fileReader);

        if (this.getSerialNumber() != 0) {
            throw new DjVuFileException(
                    "IW44 first chunk can not be initialized from IW44 chunk with not 0 serial number: "
                    + this.getSerialNumber());
        }

        this.majorVersionAndColorType = fileReader.readByte();
        this.isOneColorComponent = (this.majorVersionAndColorType & 0b1000_0000) == 0b1000_0000;
        this.majorVersion = this.majorVersionAndColorType & 0b0111_1111;
        this.minorVersion = fileReader.readByte();
        this.width = fileReader.readShort();
        this.height = fileReader.readShort();
        this.delayCounter = fileReader.readByte();
        this.chrominanceDelayCounter = this.delayCounter & 0b0111_1111;

        this.data = new byte[this.getLength() - 9 + sizeAdjustment];
        int numberOfBytesRead = fileReader.readBytes(this.data);

        if (numberOfBytesRead < this.data.length) {
            throw new DjVuFileException("Unexpected end of " + this.getChunkId() + " chunk after reading " +
                    numberOfBytesRead + " bytes");
        }

        SimpleDataLogger.logData(data, 20, "IW44 first data");
    }

    @Override
    public String toString() {
        return "Iw44FirstChunk{chunkId = " + this.getChunkId()
                + ", length = " + this.getLength()
                + ", serialNumber = " + this.getSerialNumber()
                + ", slicesNumber = " + this.getSlicesNumber()
                + ", isOneColorComponent = " + isOneColorComponent
                + ", majorVersion = " + majorVersion
                + ", minorVersion = " + minorVersion
                + ", width = " + width
                + ", height = " + height
                + ", chrominanceDelayCounter = " + chrominanceDelayCounter
                + "}";
    }
}
