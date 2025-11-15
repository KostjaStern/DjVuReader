/*
    Copyright (C) 2025 Kostya Stern

    This program is free software; you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation; either version 2 of the License, or (at your option)
    any later version.

    This program is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
    more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc., 51
    Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
*/
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

    /* Please note that these are undocumented chunks.
     *
     * TODO: The DjVu spec allows unknown chunks and says decoders must silently skip identifiers they don’t
     *       recognize — this is how LTAz could exist without breaking older software.
     **/
    LTAa, LTAz,

    /* Unicode Text and layout information */
    TXTa, TXTz,

    /* Shared shape table. */
    Djbz,

    /* BZZ compressed JB2 bitonal data used to store mask. */
    Sjbz,

    /* IW44 data used to store background (color or grayscale) */
    BG44,

    /* IW44 data used to store foreground (color or grayscale) */
    FG44,

    /* IW44 data used to store color image */
    PM44,

    /* IW44 data used to store grayscale image */
    BM44,

    /* IW44 data used to store embedded thumbnail images (color or grayscale) */
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
    private static final Set<ChunkId> TEXT_CHUNKS = EnumSet.of(TXTa, TXTz);

    public boolean isComposite() {
        return this == FORM;
    }

    public boolean isIW44Chunk() {
        return IW44_CHUNKS.contains(this);
    }

    public boolean isTextChunk() {
        return TEXT_CHUNKS.contains(this);
    }
}
