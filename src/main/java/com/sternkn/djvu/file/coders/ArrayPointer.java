package com.sternkn.djvu.file.coders;

public class ArrayPointer<T> {

    private final T[] buffer;
    private int pointer;

    public ArrayPointer(T[] buffer) {
        this.buffer = buffer;
        this.pointer = 0;
    }

    public ArrayPointer(T[] buffer, int pointer) {
        this.buffer = buffer;
        this.pointer = pointer;
    }

    public T getValue(int offset) {
        return buffer[pointer + offset];
    }

    public T getCurrentValue() {
        return buffer[pointer];
    }

    public void setValue(int offset, T value) {
        buffer[pointer + offset] = value;
    }

    public ArrayPointer<T> shiftPointer(int offset) {
        int newPointer = pointer + offset;
        return new ArrayPointer<T>(buffer, newPointer);
    }

    public void shift(int offset) {
        pointer += offset;
    }

    public boolean isCurrentValueNull() {
        return buffer[pointer] == null;
    }

    public boolean isPointerLess(ArrayPointer<T> p) {
        if (this.buffer != p.buffer) {
            throw new IllegalArgumentException("Buffer pointers must point to the same buffer.");
        }
        return this.pointer < p.pointer;
    }
}
