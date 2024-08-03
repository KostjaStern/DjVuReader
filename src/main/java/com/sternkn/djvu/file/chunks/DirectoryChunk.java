package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.DjVuFileException;
import com.sternkn.djvu.file.DjVuFileReader;
import com.sternkn.djvu.file.SimpleDataLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*
  8.3.2 Directory Chunk: DIRM

  The first contained chunk in a FORM:DJVM composite chunk is the DIRM chunk containing the document directory.
  It contains information the decoder will need to access the component files (see Multipage Documents).

  13 Appendix 4: BZZ coding
  https://codesearch.isocpp.org/actcd19/main/d/djvulibre/djvulibre_3.5.27.1-10/libdjvu/BSByteStream.cpp

  https://github.com/traycold/djvulibre/blob/master/libdjvu/BSByteStream.cpp

  https://en.wikipedia.org/wiki/Burrows%E2%80%93Wheeler_transform
 */
public class DirectoryChunk extends Chunk {
    private static final Logger LOG = LoggerFactory.getLogger(DirectoryChunk.class);

    private final boolean isBundled;
    private final int version;
    private final int nFiles;
    private final int[] offsets;
    private final int bzzDataSize;

    // The rest of the chunk is entirely compressed with the BZZ general purpose compressor.
    // (see BSByteStream.cpp and appendix 4)
    private final byte[] bzzData;


    public DirectoryChunk(ChunkId chunkId, DjVuFileReader fileReader) {
        super(chunkId, fileReader.readChunkLength());
        // fileReader.readChunkLength() -> 00 00 11 AF

        byte dirmFlags = fileReader.readByte();
        LOG.debug("dirmFlags = {}", dirmFlags); // -127 -> 81

        this.isBundled = (dirmFlags & 0b1000_0000) == 0b1000_0000;
        this.version = dirmFlags & 0b0111_1111;

        // the next two bytes of this input stream, interpreted as a signed 16-bit number
        this.nFiles = fileReader.readShort(); // 02 4C

        if (this.isBundled) {
            this.offsets = new int[this.nFiles];
            for (int ind = 0; ind < this.nFiles; ind++) {
                this.offsets[ind] = fileReader.readInt();
            }
        }
        else {
            this.offsets = new int[0];
        }
        logOffsets();

        this.bzzDataSize = this.getLength() - this.nFiles * 4 - 2;
        this.bzzData = new byte[this.bzzDataSize];
        int numberOfBytesRead = fileReader.readBytes(this.bzzData);

        if (numberOfBytesRead < this.bzzDataSize) {
            throw new DjVuFileException("Unexpected end of " + this.getChunkId() + " chunk after reading " +
                    numberOfBytesRead + " bytes");
        }

        SimpleDataLogger.logData(bzzData, 20, "bzzData");
    }

    private void logOffsets() {
        final int size = this.offsets.length;
        LOG.debug("------   offsets     ------");
        if (size < 6) {
            for (int ind = 0; ind < size; ind++) {
                LOG.debug("offsets[{}] = {}", ind, this.offsets[ind]);
            }
        }
        else {
            LOG.debug("offsets[0] = {}", this.offsets[0]);
            LOG.debug("offsets[1] = {}", this.offsets[1]);
            LOG.debug("offsets[2] = {}", this.offsets[2]);
            LOG.debug(".........................");
            LOG.debug("offsets[{}] = {}", (size - 3), this.offsets[size - 3]);
            LOG.debug("offsets[{}] = {}", (size - 2), this.offsets[size - 2]);
            LOG.debug("offsets[{}] = {}", (size - 1), this.offsets[size - 1]);
        }
        LOG.debug("-------------------------------");
    }


    @Override
    public String toString() {
        return "DirectoryChunk{chunkId = " + this.getChunkId()
                + ", length = " + this.getLength()
                + ", isBundled = " +  isBundled
                + ", version = " + version
                + ", nFiles = " + nFiles + "}";
    }
}
