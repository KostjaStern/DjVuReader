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
import com.sternkn.djvu.gui.view_model.PageNode;
import javafx.scene.image.Image;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import java.util.List;

import static com.sternkn.djvu.utils.ImageUtils.composeImage;
import static com.sternkn.djvu.utils.ImageUtils.decodeIW44Image;
import static com.sternkn.djvu.utils.ImageUtils.resizeImage;
import static com.sternkn.djvu.utils.ImageUtils.resize;
import static com.sternkn.djvu.utils.ImageUtils.toImage;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.testfx.util.WaitForAsyncUtils.asyncFx;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@ExtendWith(ApplicationExtension.class)
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
    public void testResizeImage() {
        asyncFx(() -> {
            Image src = toImage(createPixmap("Yunger_revolution.png"), ImageRotationType.NO_ROTATION);
            Image image = resizeImage(src, PageNode.WIDTH, PageNode.HEIGHT);

            Pixmap actual = new PNGPixmap(image);
            Pixmap expected = createPixmap("Yunger_revolution_resizeImage.png");

            assertPixmapEquals(expected, actual, 10);
        });

        waitForFxEvents();
    }

    @Test
    public void testResizeImageParametersValidation() {
        Image src = toImage(createPixmap("Yunger_revolution.png"), ImageRotationType.NO_ROTATION);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> resizeImage(null, PageNode.WIDTH, PageNode.HEIGHT));
        assertEquals("src is null", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class,
                () -> resizeImage(src, -1, PageNode.HEIGHT));
        assertEquals("Invalid size", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class,
                () -> resizeImage(src, PageNode.WIDTH, 0));
        assertEquals("Invalid size", exception.getMessage());
    }

    @Test
    public void testResize() {
        Image src = toImage(createPixmap("Yunger_revolution.png"), ImageRotationType.NO_ROTATION);
        Image image = resize(src, PageNode.WIDTH, PageNode.HEIGHT);

        Pixmap actual = new PNGPixmap(image);
        Pixmap expected = createPixmap("Yunger_revolution_resize.png");

        assertEquals(expected, actual);
    }

    @Test
    public void testResizeParametersValidation() {
        Image src = toImage(createPixmap("Yunger_revolution.png"), ImageRotationType.NO_ROTATION);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> resize(null, PageNode.WIDTH, PageNode.HEIGHT));
        assertEquals("src is null", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class,
                () -> resize(src, -1, PageNode.HEIGHT));
        assertEquals("Invalid size", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class,
                () -> resize(src, PageNode.WIDTH, 0));
        assertEquals("Invalid size", exception.getMessage());
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
