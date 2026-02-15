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
package com.sternkn.djvu.file.chunks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestGRectangle {

    @Test
    public void testDefaultConstructor() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> new GRectangle(5, 2, 2, 7));

        assertEquals("xmin must be less or equal to xmax", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class,
                () -> new GRectangle(2, 5, 6, 1));

        assertEquals("ymin must be less or equal to ymax", exception.getMessage());
    }

    @Test
    public void testGetWidth() {
        GRectangle rectangle = new GRectangle(2, 4, 7, 7);

        assertEquals(5, rectangle.getWidth());
    }

    @Test
    public void testGetHeight() {
        GRectangle rectangle = new GRectangle(2, 4, 7, 7);

        assertEquals(3, rectangle.getHeight());
    }

    @Test
    public void testArea() {
        GRectangle rectangle1 = new GRectangle(2, 4, 7, 7);
        assertEquals(15, rectangle1.area());

        GRectangle rectangle2 = new GRectangle(7, 4, 7, 7);
        assertEquals(0, rectangle2.area());
    }

    @Test
    public void testIsEmpty() {
        GRectangle rectangle1 = new GRectangle(2, 4, 7, 7);
        assertFalse(rectangle1.isEmpty());

        GRectangle rectangle2 = new GRectangle(7, 4, 7, 7);
        assertTrue(rectangle2.isEmpty());
    }

    @Test
    public void testIsOverlappedTrue() {
        GRectangle r1 = new GRectangle(2, 4, 7, 7);
        GRectangle r2 = new GRectangle(3, 2, 9, 6);
        GRectangle r3 = new GRectangle(4, 3, 8, 5);

        assertTrue(r1.isOverlapped(r1));

        assertTrue(r1.isOverlapped(r2));
        assertTrue(r2.isOverlapped(r1));

        assertTrue(r2.isOverlapped(r3));
        assertTrue(r3.isOverlapped(r2));
    }

    @Test
    public void testIsOverlappedFalse() {
        GRectangle r1 = new GRectangle(2, 4, 7, 7);
        GRectangle r2 = new GRectangle(3, 2, 9, 4);
        GRectangle r3 = new GRectangle(0, 0, 20, 1);

        assertFalse(r1.isOverlapped(r2));
        assertFalse(r2.isOverlapped(r1));

        assertFalse(r1.isOverlapped(r3));
        assertFalse(r3.isOverlapped(r1));
    }
}
