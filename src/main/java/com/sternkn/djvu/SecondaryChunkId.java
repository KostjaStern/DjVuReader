package com.sternkn.djvu;

public enum SecondaryChunkId {

    /*
        A multipage DjVu document. Composite chunk that contains the DIRM chunk,
        possibly shared/included chunks and subsequent FORM:DJVU chunks which make up a multipage document.
     */
    DJVM,

    /*
        A DjVu Page / single page DjVu document. Composite chunk that contains the chunks
        which make up a page in a djvu document.
     */
    DJVU,

    /*
        A "shared" DjVu file which is included via the INCL chunk.
        Shared annotations, shared shape dictionary.
     */
    DJVI,

    /*
        Composite chunk that contains the TH44 chunks which are the embedded thumbnails
     */
    THUM;
}
