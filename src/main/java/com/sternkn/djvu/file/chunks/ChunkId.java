package com.sternkn.djvu.file.chunks;

import java.util.EnumSet;
import java.util.Set;

public enum ChunkId {

    /* The composite chunk. */
    FORM,

    /* Page name information for multi-page documents */
    DIRM,

    /* Bookmark information */
    NAVM,

    /* Annotations including both initial view settings and overlaid
       hyperlinks, text boxes, etc. */
    ANTa, ANTz,

    /* Unicode Text and layout information */
    TXTa, TXTz,

    /* Shared shape table. */
    Djbz,

    /* BZZ compressed JB2 bitonal data used to store mask. */
    Sjbz,

    /* IW44 data used to store background */
    BG44,

    /* IW44 data used to store foreground */
    FG44,

    /* IW44 data used to store color image */
    PM44,

    /* IW44 data used to store grayscale image */
    BM44,

    /* IW44 data used to store embedded thumbnail images */
    TH44,

    /* JB2 data required to remove a watermark */
    WMRM,

    /* Color JB2 data. Provides a color for each (blit or shape?) in the corresponding Sjbz chunk. */
    FGbz,

    /* Information about a DjVu page */
    INFO,

    /* The ID of an included FORM:DJVI chunk. */
    INCL,

    /* JPEG encoded background */
    BGjp,

    /* JPEG encoded foreground */
    FGjp,

    /* G4 encoded mask */
    Smmr,

    /*
         The chunk "CIDa" is not documented in djvu spec.
         Obsolete chunk with unknown content. (according to https://en.wikipedia.org/wiki/DjVu)
     */
    CIDa;

    private static final Set<ChunkId> IW44_CHUNKS = EnumSet.of(BG44, FG44, PM44, BM44, TH44);

    public boolean isComposite() {
        return this == FORM;
    }

    public boolean isIW44Chunk() {
        return IW44_CHUNKS.contains(this);
    }
}
