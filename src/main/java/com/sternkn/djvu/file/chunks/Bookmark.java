package com.sternkn.djvu.file.chunks;

public record Bookmark(int nChildren, int nDesc, String sDesc, int nURL, String sURL) {

    public Bookmark(int nDesc, String sDesc, int nURL, String sURL) {
        this(0, nDesc, sDesc, nURL, sURL);
    }
}
