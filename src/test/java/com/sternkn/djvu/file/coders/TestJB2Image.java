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

import com.sternkn.djvu.file.DjVuFileException;
import com.sternkn.djvu.file.chunks.Chunk;
import com.sternkn.djvu.file.chunks.ChunkId;
import com.sternkn.djvu.file.chunks.FGbzChunk;
import com.sternkn.djvu.file.chunks.ImageRotationType;
import com.sternkn.djvu.utils.PNGPixmap;
import javafx.scene.image.Image;
import org.junit.jupiter.api.Test;

import static com.sternkn.djvu.utils.ImageUtils.decodeJB2Image;
import static com.sternkn.djvu.utils.ImageUtils.toImage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestJB2Image extends TestSupport {

    @Test
    public void testGetBitmap() {
        JB2Image image = readImage("Sjbz_16.data", "Djbz_4.data");
        GBitmap bitmap = image.get_bitmap();

        assertEquals(4539, bitmap.getHeight());
        assertEquals(2832, bitmap.getWidth());

        BufferPointer row0 = bitmap.getRow(0);
        for (int x = 0; x < bitmap.getWidth(); x++) {
            int row0x = row0.getValue(x);
            // 2724 .. 2736
            if (x >= 2724 && x <= 2736) {
                assertEquals(1, row0x);
            }
            else {
                assertEquals(0, row0x);
            }
        }
    }

    @Test
    public void testFullBitmap() {
        JB2Image image = readImage("Sjbz_ddd.data", "Djbz_ddd.data");
        Pixmap actualPixmap = image.get_bitmap();
        Pixmap expectedPixmap = readPixmap("Sjbz_out_ddd.png");

        assertPixmapEquals(expectedPixmap, actualPixmap);
    }

    /*
    ERROR c.s.d.g.view_model.ChunkDecodingTask - com.sternkn.djvu.file.DjVuFileException: GBitmap.right_damaged
	at com.sternkn.djvu.file.coders.GBitmap.check_border(GBitmap.java:154)
	at com.sternkn.djvu.file.coders.JB2CodecDecoder.code_bitmap_by_cross_coding(JB2CodecDecoder.java:622)
	at com.sternkn.djvu.file.coders.JB2CodecDecoder.codeRecord(JB2CodecDecoder.java:227)
	at com.sternkn.djvu.file.coders.JB2CodecDecoder.decode(JB2CodecDecoder.java:145)
	at com.sternkn.djvu.utils.ImageUtils.decodeJB2Image(ImageUtils.java:243)
	at com.sternkn.djvu.model.DjVuModelImpl.getBitonalImage(DjVuModelImpl.java:240)
	at com.sternkn.djvu.model.DjVuModelImpl.getBitonalChunkInfo(DjVuModelImpl.java:223)
	at com.sternkn.djvu.model.DjVuModelImpl.getChunkInfo(DjVuModelImpl.java:119)
	at com.sternkn.djvu.gui.view_model.ChunkDecodingTask.call(ChunkDecodingTask.java:42)
	at com.sternkn.djvu.gui.view_model.ChunkDecodingTask.call(ChunkDecodingTask.java:28)
	at javafx.concurrent.Task$TaskCallable.call(Task.java:1401)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:317)
	at java.base/java.lang.Thread.run(Thread.java:1583)
     */
    @Test
    public void testJB2Image() {
        Exception exception = assertThrows(DjVuFileException.class,
                () -> readImage("Vinogradov_Sjbz.data"));
        assertEquals("GBitmap.right_damaged", exception.getMessage());
    }

    @Test
    public void testJB2ImageWithForegroundColors() {
        JB2Image jb2Image = readImage("Yunger_Sjbz.data");

        Chunk chunk = readChunk("Yunger_FGbz.data", ChunkId.FGbz);
        FGbzChunk foregroundColorsChunk = new FGbzChunk(chunk);

        Pixmap pixmap = jb2Image.get_bitmap(foregroundColorsChunk);
        Image image = toImage(pixmap, ImageRotationType.UPSIDE_DOWN);

        Pixmap actualPixmap = new PNGPixmap(image);

        Pixmap expectedPixmap = readPixmap("Yunger.png");
        assertPixmapEquals(expectedPixmap, actualPixmap);
    }

    @Test
    public void testJB2ImageWithForegroundColorsWithInvalidPaletteIndex() {
        JB2Image maskImage = decodeJB2Image(readByteBuffer("Evans_Sjbz.data"),
                readByteBuffer("Evans_Djbz.data"));
        Chunk chunk = readChunk("Evans_FGbz.data", ChunkId.FGbz);
        FGbzChunk foregroundColorsChunk = new FGbzChunk(chunk);

        Pixmap actualPixmap = maskImage.get_bitmap(foregroundColorsChunk);

        Pixmap expectedPixmap = readPixmap("Evans.png");
        assertPixmapEquals(expectedPixmap, actualPixmap);
    }
}
