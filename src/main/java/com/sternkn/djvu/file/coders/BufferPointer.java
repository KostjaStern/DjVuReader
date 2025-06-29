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

    public BufferPointer shiftPointer(int offset) {
        int newPointer = pointer + offset;
        return new BufferPointer(buffer, newPointer);
    }

    public boolean isCurrentValueZero() {
        return buffer[pointer] == 0;
    }

    public boolean isPointerLess(BufferPointer p) {
        if (this.buffer != p.buffer) {
            throw new IllegalArgumentException("Buffer pointers must point to the same buffer.");
        }
        return this.pointer < p.pointer;
    }
}
