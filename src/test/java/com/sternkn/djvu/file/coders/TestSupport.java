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

import com.sternkn.djvu.file.chunks.Chunk;
import com.sternkn.djvu.file.chunks.ChunkId;
import com.sternkn.djvu.utils.PNGPixmap;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSupport {
    private static final String PATH_CHUNKS = "test_chunks/";
    private static final String PATH_IMAGES = "test_images/";

    private final ClassLoader classLoader = getClass().getClassLoader();

    public PNGPixmap createPixmap(String filename) {
        return new PNGPixmap(new File("src/test/resources/test_images/" + filename));
    }

    public GPixmap readPixmap(String fileName) {
        try (InputStream inputStream = classLoader.getResourceAsStream(PATH_IMAGES + fileName)) {
            BufferedImage image = ImageIO.read(Objects.requireNonNull(inputStream));
            final int width = image.getWidth();
            final int height = image.getHeight();
            GPixmap pixmap = new GPixmap(height, width);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int argb = image.getRGB(x, y);

                    final int red   = (argb >> 16) & 0xFF;
                    final int green = (argb >> 8)  & 0xFF;
                    final int blue  = argb         & 0xFF;
                    PixelColor color = new PixelColor(blue, green, red);
                    pixmap.setPixel(x, y, color);
                }
            }

            return pixmap;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void assertPixmapEquals(Pixmap expected, Pixmap actual) {
        assertEquals(expected.getWidth(), actual.getWidth());
        assertEquals(expected.getHeight(), actual.getHeight());
        for (int y = 0; y < actual.getHeight(); y++) {
            for (int x = 0; x < actual.getWidth(); x++) {
                assertEquals(expected.getPixel(x, y), actual.getPixel(x, y));
            }
        }
    }

    public void assertPixmapEquals(Pixmap expected, Pixmap actual, int colorDelta) {
        assertEquals(expected.getWidth(), actual.getWidth());
        assertEquals(expected.getHeight(), actual.getHeight());
        for (int y = 0; y < actual.getHeight(); y++) {
            for (int x = 0; x < actual.getWidth(); x++) {
                PixelColor expectedColor = expected.getPixel(x, y);
                PixelColor actualColor = actual.getPixel(x, y);
                boolean isColorsEquals = (Math.abs(actualColor.getBlue() - expectedColor.getBlue()) <= colorDelta)
                        && (Math.abs(actualColor.getGreen() - expectedColor.getGreen()) <= colorDelta)
                        && (Math.abs(actualColor.getRed() - expectedColor.getRed()) <= colorDelta);

                assertTrue(isColorsEquals,
                        "Colors are not equal[expectedColor = " + expectedColor +
                                ", actualColor = " + actualColor + "]");
            }
        }
    }

    public InputStream readStream(String fileName) {
        return classLoader.getResourceAsStream(PATH_CHUNKS + fileName);
    }

    public byte[] readByteBuffer(String fileName) {
        try (InputStream inputStream = readStream(fileName)) {
            return inputStream != null ? inputStream.readAllBytes() : new byte[0];
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Chunk readChunk(String fileName, ChunkId chunkId) {
        return readChunk(fileName, chunkId, null, 0L);
    }

    public Chunk readChunk(String fileName, ChunkId chunkId, Chunk parent, Long offsetStart) {
        return readChunk(0L, fileName, chunkId, parent, offsetStart);
    }

    public Chunk readChunk(Long id, String fileName, ChunkId chunkId, Chunk parent, Long offsetStart) {
        byte[] buffer = readByteBuffer(fileName);

        return Chunk.builder()
                .withId(id)
                .withChunkId(chunkId)
                .withData(buffer)
                .withParent(parent)
                .withOffsetStart(offsetStart)
                .withSize(buffer.length).build();
    }

    JB2Image readImage(String imageFileName) {
        return readImage(imageFileName, null);
    }

    JB2Image readImage(String imageFileName, String dictFileName) {
        JB2Dict dict = dictFileName != null ? readDictionary(dictFileName) : null;
        JB2Image image = new JB2Image();
        image.setInheritedDictionary(dict);

        try (InputStream inputStream = readStream(imageFileName)) {
            JB2CodecDecoder decoder = new JB2CodecDecoder(inputStream);
            decoder.decode(image);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        return image;
    }

    JB2Dict readDictionary(String fileName) {
        JB2Dict dict = new JB2Dict();

        try (InputStream inputStream = readStream(fileName)) {
            JB2CodecDecoder decoder = new JB2CodecDecoder(inputStream);
            decoder.decode(dict);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        return dict;
    }
}
