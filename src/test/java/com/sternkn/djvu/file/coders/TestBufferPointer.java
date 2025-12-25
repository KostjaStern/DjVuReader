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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestBufferPointer {

    @Test
    public void testGetValue() {
        final int[] buffer = {1, 2, 3, 4, 5, 6};
        final int pointer = 2;
        BufferPointer bufferPointer = new BufferPointer(buffer, pointer);

        assertEquals(buffer[pointer + 1], bufferPointer.getValue(1));
        assertEquals(buffer[pointer + 2], bufferPointer.getValue(2));
        assertEquals(buffer[pointer + 3], bufferPointer.getValue(3));
    }

    @Test
    public void testGetCurrentValue() {
        final int[] buffer = {4, 5, 1, -2, 23, 72};
        final int pointer = 3;
        BufferPointer bufferPointer = new BufferPointer(buffer, pointer);

        assertEquals(buffer[pointer], bufferPointer.getCurrentValue());
    }

    @Test
    public void testShiftPointer() {
        final int[] buffer = {4, 5, 1, -2, 23, 72};
        final int pointer = 1;
        final int offset = 2;

        BufferPointer bufferPointer = new BufferPointer(buffer, pointer);
        BufferPointer shiftPointer = bufferPointer.shiftPointer(offset);

        assertSame(buffer, shiftPointer.getBuffer());
        assertSame(buffer, bufferPointer.getBuffer());
        assertEquals(pointer, bufferPointer.getPointer());
        assertEquals(pointer + offset, shiftPointer.getPointer());
    }

    @Test
    public void testIsPointerLessIllegalArgument() {
        BufferPointer bp1 = new BufferPointer(new int[]{1, 2, 3}, 1);
        BufferPointer bp2 = new BufferPointer(new int[]{1, 2, 3}, 2);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> bp1.isPointerLess(bp2));
        assertEquals("Buffer pointers must point to the same buffer.", exception.getMessage());
    }

    @Test
    public void testIsPointerLessPositive() {
        final int [] buffer = {1, 2, 3};
        BufferPointer bp1 = new BufferPointer(buffer, 1);
        BufferPointer bp2 = new BufferPointer(buffer, 2);

        assertTrue(bp1.isPointerLess(bp2));
    }

    @Test
    public void testIsPointerLessNegative() {
        final int [] buffer = {1, 2, 3};
        BufferPointer bp1 = new BufferPointer(buffer, 2);
        BufferPointer bp2 = new BufferPointer(buffer, 2);

        assertFalse(bp1.isPointerLess(bp2));
    }

    @Test
    public void testCopy() {
        int[] srcBuffer = {1, 2, 3, 4, 5, 6};
        int[] dstBuffer = {1, 1, 0, 0, 0, 0, 23, 3};
        BufferPointer src = new BufferPointer(srcBuffer, 2);
        BufferPointer dst = new BufferPointer(dstBuffer, 1);

        BufferPointer.copy(dst, src, 4);

        int[] expectedBuffer = {1, 3, 4, 5, 6, 0, 23, 3};
        assertArrayEquals(expectedBuffer, dst.getBuffer());
    }
}
