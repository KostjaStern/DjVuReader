package com.sternkn.djvu.file.coders;

public class BufferPointer {

    private int[] buffer;
    private int pointer;

    public BufferPointer(int[] buffer, int pointer) {
        this.buffer = buffer;
        this.pointer = pointer;
    }

    public int getValue(int offset) {
        return buffer[pointer + offset];
    }

    public void setValue(int offset, int value) {
        buffer[pointer + offset] = value;
    }
}
