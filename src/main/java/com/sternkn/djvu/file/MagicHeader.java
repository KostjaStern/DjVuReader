package com.sternkn.djvu.file;

public enum MagicHeader {
    AT_T("AT&T"),
    SDJV("SDJV");

    private final String header;

    MagicHeader(String header) {
        this.header = header;
    }

    public static MagicHeader of(String header) {
        for (MagicHeader magicHeader : MagicHeader.values()) {
            if (magicHeader.header.equals(header)) {
                return magicHeader;
            }
        }

        throw new DjVuFileException(String.format("Unexpected magic file header: %s", header));
    }
}
