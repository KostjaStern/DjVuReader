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

public class TestImageRotationType {

    @Test
    public void testGetRotationTypeValidFlag() {
        final byte flag = 0b0111_0101;
        ImageRotationType rotationType = ImageRotationType.getRotationType(flag);

        assertEquals(ImageRotationType.CLOCKWISE_90, rotationType);
    }

    @Test
    public void testGetRotationTypeForUnknownFlag() {
        assertEquals(ImageRotationType.NO_ROTATION, ImageRotationType.getRotationType(0));
    }
}
