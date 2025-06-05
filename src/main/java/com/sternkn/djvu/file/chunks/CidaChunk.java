package com.sternkn.djvu.file.chunks;

/*
    We think that the unrecognized chunk "CIDa" is created by the Virtual Print Driver.
    This unrecognized chunk was not present in previous versions of the DjVu file format.
    It contains the string "msepdjvu3.6.1" followed by some additional binary information.
    The exact purpose of the additional binary data and of the purpose of this chunk in general is unknown to us.
 */
public class CidaChunk {

    private byte[] data;
    private String dataAsString;

    public CidaChunk() {
    }
}
