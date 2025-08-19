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
