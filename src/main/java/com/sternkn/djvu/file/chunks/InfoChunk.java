package com.sternkn.djvu.file.chunks;

/*
    As discussed in Single Page Documents, every DjVu image requires an INFO chunk and
    this must be the first (non-container) chunk. The INFO chunk data consists of seven fields in 10 bytes
 */
public class InfoChunk {

    // width of the image in pixels
    private short width;

    // height of the image in pixels
    private short height;

    // the minor version number of the encoder being used (currently 26)
    private byte minorVersion;

    // the major version number of the encoder being used (currently 0)
    private byte majorVersion;

    // the spatial resolution of the image in dots per inch (dots per 2.54 cm)
    private short dpi;

    // 10 times the gamma of the device on which the image is expected to be rendered
    private byte gamma;

    /*
        Mask to be interpretted as follows:
        The first 5 bits are reserved for future implementations
        The last 3 bits specify the image’s rotation. The following 4 patterns are recognized:
            1 – 0° (rightside up)
            6 – 90° Counter Clockwise
            2 – 180° (unside down)
            5 – 90° Clockwise
        Note that the rotation affects the any coordinates in the Annotation chunk.
     */
    private byte flags;

    private ImageRotationType rotation;

    public InfoChunk() {
//        super(chunkId, fileReader.readChunkLength());
//
//        this.width = fileReader.readShort();
//        this.height = fileReader.readShort();
//        this.minorVersion = fileReader.readByte();
//        this.majorVersion = fileReader.readByte();
//        this.dpi = fileReader.readShort();
//        this.gamma = fileReader.readByte();
//        this.flags = fileReader.readByte();
//
//        this.rotation = ImageRotationType.getRotationType(this.flags);
    }

//    @Override
//    public String toString() {
//        return "InfoChunk{chunkId = " + this.getChunkId()
//                    + ", length = " + this.getLength()
//                    + ", width = " + this.width
//                    + ", height = " + this.height
//                    + ", minorVersion = " + this.minorVersion
//                    + ", majorVersion = " + this.majorVersion
//                    + ", dpi = " + this.dpi
//                    + ", gamma = " + this.gamma
//                    + ", flags = " + this.flags
//                    + ", rotation = " + this.rotation + "}";
//    }
}
