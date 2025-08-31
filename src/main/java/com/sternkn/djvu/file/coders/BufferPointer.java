package com.sternkn.djvu.file.coders;

public class BufferPointer {

    private final int[] buffer;
    private int pointer;

    public BufferPointer(BufferPointer bufferPointer) {
        this.buffer = bufferPointer.buffer;
        this.pointer = bufferPointer.pointer;
    }

    public BufferPointer(int[] buffer) {
        this.buffer = buffer;
        this.pointer = 0;
    }

    public BufferPointer(int[] buffer, int pointer) {
        this.buffer = buffer;
        this.pointer = pointer;
    }

    public int getValue(int offset) {
        return buffer[pointer + offset];
    }

    public int getCurrentValue() {
        return buffer[pointer];
    }

    public void setValue(int offset, int value) {
        buffer[pointer + offset] = value;
    }

    public BufferPointer shiftPointer(int offset) {
        int newPointer = pointer + offset;
        return new BufferPointer(buffer, newPointer);
    }

    public void shift(int offset) {
        pointer += offset;
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
