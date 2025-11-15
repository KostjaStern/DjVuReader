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

public enum ComponentType {

    /* This is a top level page file. It may include other {INCLUDE} files,
       which may in turn be shared between different pages.
    */
    PAGE,

    /* This file contains thumbnails for the document pages. */
    THUMBNAIL,

    /* This file is included into some other file inside this document. */
    INCLUDED,

    /* This file contains annotations shared by all the pages.
       It's supposed to be included into every page for the annotations to take effect.
       There may be only one file with shared annotations in a document.
     */
    SHARED_ANNO;

    private final static int TYPE_MASK = 0x3f;

    public static ComponentType valueOf(int value) {
        final int flag = value & TYPE_MASK;
        return switch (flag) {
            case 0 -> INCLUDED;
            case 1 -> PAGE;
            case 2 -> THUMBNAIL;
            case 3 -> SHARED_ANNO;
            default -> throw new IllegalStateException("Invalid flag: " + value);
        };
    }
}
