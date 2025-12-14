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
package com.sternkn.djvu.utils;

import com.sternkn.djvu.file.chunks.ImageRotationType;
import com.sternkn.djvu.file.coders.IW44Image;
import com.sternkn.djvu.file.coders.Pixmap;
import com.sternkn.djvu.file.coders.TestSupport;
import javafx.scene.image.Image;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.sternkn.djvu.utils.ImageUtils.composeImage;
import static com.sternkn.djvu.utils.ImageUtils.decodeIW44Image;
import static com.sternkn.djvu.utils.ImageUtils.toImage;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestImageUtils extends TestSupport {

    @Test
    public void testToImageCounterClockwise90() {
        Pixmap pixmap = createPixmap("Dudaev.png");
        Image image = toImage(pixmap, ImageRotationType.COUNTER_CLOCKWISE_90);

        Pixmap actual = new PNGPixmap(image);
        Pixmap expected = createPixmap("Dudaev_COUNTER_CLOCKWISE_90.png");

        assertEquals(expected, actual);
    }

    @Test
    public void testToImageUpsideDown() {
        Pixmap pixmap = createPixmap("Dudaev.png");
        Image image = toImage(pixmap, ImageRotationType.UPSIDE_DOWN);

        Pixmap actual = new PNGPixmap(image);
        Pixmap expected = createPixmap("Dudaev_UPSIDE_DOWN.png");

        assertEquals(expected, actual);
    }

    @Test
    public void testToImageClockwise90() {
        Pixmap pixmap = createPixmap("Dudaev.png");
        Image image = toImage(pixmap, ImageRotationType.CLOCKWISE_90);

        Pixmap actual = new PNGPixmap(image);
        Pixmap expected = createPixmap("Dudaev_CLOCKWISE_90.png");

        assertEquals(expected, actual);
    }

    @Test
    public void testComposeImage() {
        PNGPixmap mask = createPixmap("Dudaev_Sjbz.png");
        PNGPixmap background = createPixmap("Dudaev_BG44.png");
        PNGPixmap foreground = createPixmap("Dudaev_FG44.png");

        Image image = composeImage(mask, background, foreground, mask.getHeight(), mask.getWidth());

        Pixmap actual = new PNGPixmap(image);
        Pixmap expected = createPixmap("Dudaev.png");

        assertEquals(expected, actual);
    }

    @Test
    public void testDecodeGrayscaleIW44Image() {
        byte[] data = readByteBuffer("BG44_grayscale.data");

        IW44Image image = decodeIW44Image(List.of(data));
        Pixmap actual = image.get_pixmap();
        Pixmap expected = createPixmap("BG44_grayscale.png");

        assertPixmapEquals(expected, actual);
    }
}
