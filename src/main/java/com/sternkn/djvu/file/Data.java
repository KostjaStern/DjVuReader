package com.sternkn.djvu.file;

public record Data(byte[] buffer, int size) {

    public int getFirstByte() {
        return size > 0 ? buffer[0] : 0xFF;
    }
}
