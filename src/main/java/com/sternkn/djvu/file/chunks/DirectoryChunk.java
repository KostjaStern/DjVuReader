package com.sternkn.djvu.file.chunks;

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
public class DirectoryChunk {
    private static final Logger LOG = LoggerFactory.getLogger(DirectoryChunk.class);

    private boolean isBundled;
    private int version;
    private int nFiles;
    private int[] offsets;
    // private final int bzzDataSize;

    // The rest of the chunk is entirely compressed with the BZZ general purpose compressor.
    // (see BSByteStreamDecode.cpp and appendix 4)
    // private final byte[] bzzData;

    private int indexBzzData;

//    private int currentByte;  // unsigned char byte;
//    private byte delay;        // unsigned char delay;
//    private byte scount;       // unsigned char scount;
//    private int code;          // unsigned int  code;
//    private int a;             // unsigned int  a;

    public DirectoryChunk() {
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
}
