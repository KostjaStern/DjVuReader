package com.sternkn.djvu.file.coders;

public class BitContext {
    private int value;

    public BitContext() {
        value = 0;
    }

    public int getValue() {
        return value;
    }
    public void setValue(int value) {
        this.value = value;
    }
}
