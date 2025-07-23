package com.sternkn.djvu.file.chunks;


import com.sternkn.djvu.file.utils.ByteOrder;

import java.io.ByteArrayInputStream;

import static com.sternkn.djvu.file.utils.InputStreamUtils.read16;
import static com.sternkn.djvu.file.utils.StringUtils.NL;

/*
    As discussed in Single Page Documents, every DjVu image requires an INFO chunk and
    this must be the first (non-container) chunk. The INFO chunk data consists of seven fields in 10 bytes
*/
public class InfoChunk extends Chunk {

    // width of the image in pixels
    private final int width;

    // height of the image in pixels
    private final int height;

    // the minor version number of the encoder being used (currently 26)
    private final int minorVersion;

    // the major version number of the encoder being used (currently 0)
    private final int majorVersion;

    // the spatial resolution of the image in dots per inch (dots per 2.54 cm)
    private final int dpi;

    // 10 times the gamma of the device on which the image is expected to be rendered
    private final int gamma;

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
    private final int flags;

    private final ImageRotationType rotation;

    public InfoChunk(Chunk chunk) {
        super(chunk);
        final ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
        this.width = read16(byteStream);
        this.height = read16(byteStream);
        this.minorVersion = byteStream.read();
        this.majorVersion = byteStream.read();
        this.dpi = read16(byteStream, ByteOrder.LITTLE_ENDIAN);
        this.gamma = byteStream.read();
        this.flags = byteStream.read();

        this.rotation = ImageRotationType.getRotationType(this.flags);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getDpi() {
        return dpi;
    }

    public int getGamma() {
        return gamma;
    }

    public ImageRotationType getRotation() {
        return rotation;
    }

    @Override
    public String getDataAsText() {
        String parentData = super.getDataAsText();

        StringBuilder buffer = new StringBuilder();
        buffer.append(parentData);

        buffer.append(" Width: ").append(width).append(NL);
        buffer.append(" Height: ").append(height).append(NL);
        buffer.append(" Minor version: ").append(minorVersion).append(NL);
        buffer.append(" Major version: ").append(majorVersion).append(NL);
        buffer.append(" DPI: ").append(dpi).append(NL);
        buffer.append(" Gamma: ").append(gamma).append(NL);
        buffer.append(" Rotation: ").append(rotation).append(NL);

        return  buffer.toString();
    }

        @Override
    public String toString() {
        return "InfoChunk{chunkId = " + this.getChunkId()
                    + ", size = " + this.getSize()
                    + ", width = " + this.width
                    + ", height = " + this.height
                    + ", minorVersion = " + this.minorVersion
                    + ", majorVersion = " + this.majorVersion
                    + ", dpi = " + this.dpi
                    + ", gamma = " + this.gamma
                    + ", flags = " + this.flags
                    + ", rotation = " + this.rotation + "}";
    }
}
