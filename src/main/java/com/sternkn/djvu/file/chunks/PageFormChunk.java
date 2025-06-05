package com.sternkn.djvu.file.chunks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
    FORM:DJVU
    A DjVu Page / single page DjVu document. Composite chunk that contains the chunks
    which make up a page in a djvu document.

    The nested first chunk must be the INFO chunk.
    The chunks after the INFO chunk may occur in any order, although the order of the BG44 chunks,
    if there is more than one, is significant.
 */
public class PageFormChunk {
    private static final Logger LOG = LoggerFactory.getLogger(PageFormChunk.class);

    private InfoChunk infoChunk;
    // private final byte[] data;

    public PageFormChunk() {
    }

    public InfoChunk getInfoChunk() {
        return this.infoChunk;
    }
}
