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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public interface Pixmap {

    int getHeight();

    int getWidth();

    int getBorder();

    PixelColor getPixel(int x, int y);

    default void save(String filename) throws IOException {
        BufferedImage buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                PixelColor pixel = getPixel(x, y);

                int rgb = pixel.getBlue() | (pixel.getGreen() << 8) | (pixel.getRed() << 16);
                buffer.setRGB(x, y, rgb);
            }
        }

        File outputfile = new File(filename);
        ImageIO.write(buffer, "png", outputfile);
    }
}
