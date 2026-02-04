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

public class TestJB2CodecDecoder extends TestSupport {

    @Test
    public void testDecodeImageWithoutDictionary() {
        JB2Image image = readImage("sample1_Sjbz_9.data");

        assertEquals(2600, image.getWidth());
        assertEquals(4200, image.getHeight());
        assertEquals(1861, image.get_blit_count());
        assertEquals(120, image.get_shape_count());
        assertEquals(0, image.get_inherited_shape_count());

        assertEquals(new JB2Blit(1539, 4049, 0), image.get_blit(0));
        assertEquals(new JB2Blit(1569, 4048, 1), image.get_blit(1));
        assertEquals(new JB2Blit(1596, 4048, 2), image.get_blit(2));
        assertEquals(new JB2Blit(1800, 4029, 117), image.get_blit(1858));
        assertEquals(new JB2Blit(2100, 4029, 118), image.get_blit(1859));
        assertEquals(new JB2Blit(2400, 4029, 119), image.get_blit(1860));
    }

    @Test
    public void testDecodeImageWithSharedAnnotation() {
        JB2Image image = readImage("Sjbz_with_record_type_6.data");

        assertEquals(1871, image.getWidth());
        assertEquals(2633, image.getHeight());
        assertEquals(0, image.get_blit_count());
        assertEquals(0, image.get_shape_count());
        assertEquals(0, image.get_inherited_shape_count());
    }

    @Test
    public void testDecodeImageWithSharedAnnotation2() {
        JB2Image image = readImage("Sjbz_with_record_type_6_2.data");

        assertEquals(1871, image.getWidth());
        assertEquals(2638, image.getHeight());
        assertEquals(1259, image.get_blit_count());
        assertEquals(351, image.get_shape_count());
        assertEquals(0, image.get_inherited_shape_count());
    }

    @Test
    public void testDecodeImage() {
        JB2Image image = readImage("Sjbz_16.data", "Djbz_4.data");

        assertEquals(2832, image.getWidth());
        assertEquals(4539, image.getHeight());
        assertEquals(241, image.get_blit_count());
        assertEquals(707, image.get_shape_count());
        assertEquals(488, image.get_inherited_shape_count());

        assertEquals(new JB2Blit(1285, 4025, 488), image.get_blit(0));
        assertEquals(new JB2Blit(1329, 4011, 489), image.get_blit(1));
        assertEquals(new JB2Blit(1840, 4017, 490), image.get_blit(2));
        assertEquals(new JB2Blit(2000, 62, 704), image.get_blit(238));
        assertEquals(new JB2Blit(1000, 65, 705), image.get_blit(239));
        assertEquals(new JB2Blit(1500, 66, 706), image.get_blit(240));
    }

    @Test
    public void testDecodeDictionary() {
        JB2Dict dict = readDictionary("Djbz_4.data");

        assertEquals(488, dict.get_shape_count());

        assertEquals(34, dict.get_shape(0).getBits().getWidth());
        assertEquals(41, dict.get_shape(0).getBits().getHeight());
        assertEquals(32, dict.get_shape(1).getBits().getWidth());
        assertEquals(38, dict.get_shape(1).getBits().getHeight());

        assertEquals(5, dict.get_shape(486).getBits().getWidth());
        assertEquals(7, dict.get_shape(486).getBits().getHeight());
        assertEquals(22, dict.get_shape(487).getBits().getWidth());
        assertEquals(21, dict.get_shape(487).getBits().getHeight());
    }

    @Test
    public void testDecodeDictionaryWithComment() {
        JB2Dict dict = readDictionary("My_Djbz_with_comment.data");

        assertEquals("My dict comment!", dict.getComment());

        assertEquals(1, dict.get_shape_count());

        JB2Shape shape = dict.get_shape(0);

        final int[] bytes_data = {
            0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        };
        final int[] buffer = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        final GBitmap expectedBitmap = new GBitmap();
        expectedBitmap.init(5, 3, 4, bytes_data, buffer);

        assertPixmapEquals(expectedBitmap, shape.getBits());
    }
}
