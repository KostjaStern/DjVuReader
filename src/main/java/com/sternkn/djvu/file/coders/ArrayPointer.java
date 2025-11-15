/*
    Copyright (C) 2025 Kostya Stern

    This program is free software; you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation; either version 2 of the License, or (at your option)
    any later version.

    This program is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
    more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc., 51
    Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
*/
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
