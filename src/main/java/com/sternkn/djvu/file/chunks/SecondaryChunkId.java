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
