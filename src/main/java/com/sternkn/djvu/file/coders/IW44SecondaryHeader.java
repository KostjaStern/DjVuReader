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

import java.io.InputStream;

import static com.sternkn.djvu.utils.utils.InputStreamUtils.read16;
import static com.sternkn.djvu.utils.utils.InputStreamUtils.read8;

public class IW44SecondaryHeader {
    private static final int IWCODEC_MAJOR = 1;
    private static final int IWCODEC_MINOR = 2;

    /*
       Major version number and color type. One octet containing two values, present only if
       the serial number is 0.

       The least significant seven bits designate the major version number of the standard being implemented
       by the decoder. For this version of the standard, the major version number is 1.
     */
    private final int majorVersion;

    /*
       The most significant bit is the color type bit. The color type bit is 0 if the chunk describes three
       color components. The color type bit is 1 if the chunk describes one color component.
     */
    private final int colorType;

    /*
       Minor version number. A one-octet unsigned integer, present only if the serial umber is 0.
       This octet designates the minor version number of the standard being implemented by the
       decoder. For this version of the standard, the minor version number is 2.
     */
    private final int minorVersion;

    /*
       A two-octet unsigned integer, most significant octet first, present only if the serial number is 0.
       This field indicates the number of pixels in each row of the image described by the current chunk.
       The image width will be less than the width of the original image if the chunk describes a layer coded
       at lower resolution than the original image. For a BG44 or FG44 chunk, if W is the width of the original
       image specified in the INFO chunk, and w is the width of the image described by the current chunk, then the
       allowable values of w are:
         [W/1] , [W/2] , [W/3] , [W/4] , [W/5] , [W/6] , [W/7] , [W/8] , [W/9] , [W/10] , [W/11] and [W/12]
       For a BM44 or PM44 chunk, there are no restrictions on the image width.
     */
    private final int width;

    /*
       A two-octet unsigned integer, most significant octet first, present only if the serial number is 0.
       This field indicates the number of pixels in each column of the image described by the current chunk.
       The image height will be less than the height of the original image if the chunk describes a layer coded
       at lower resolution than the original image. For a BG44 or FG44 chunk, if H is the height of the original
       image specified in the INFO chunk, and h is the height of the image described by the current
       chunk, then the allowable values of h are:
         [H/1] , [H/2] , [H/3] , [H/4] , [H/5] , [H/6] , [H/7] , [H/8] , [H/9] , [H/10] , [H/11] and [H/12]
       For a BG44 or FG44 chunk, It must be the case that
         [W/w] = [H/h]
       For a BM44 or PM44 chunk, there are no restrictions on the image width.
     */
    private final int height;

    /*
       Initial value of chrominance delay counter. A one-octet unsigned integer, present only if the serial number
       is 0. Only the least significant seven bits are used. The most significant bit is ignored, but should be
       set to 1 by an encoder.
     */
    private final int chrominanceDelay;

    private final int crcbHalf;

    public IW44SecondaryHeader(InputStream inputStream) {
        int major = read8(inputStream);

        majorVersion = major & 0x7f;
        colorType    = major & 0x80;
        minorVersion = read8(inputStream);

        if (majorVersion != IWCODEC_MAJOR) {
            throw new DjVuFileException("IW44Image.incompat_codec");
        }
        if (minorVersion > IWCODEC_MINOR) {
            throw new DjVuFileException("IW44Image.recent_codec");
        }

        width = read16(inputStream);
        height = read16(inputStream);

        int value = read8(inputStream);
        chrominanceDelay = (colorType != 0) ? -1 : value & 0x7f;
        crcbHalf = ((value & 0x80) != 0) ? 0 : 1;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public int getColorType() {
        return colorType;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getChrominanceDelay() {
        return chrominanceDelay;
    }

    public int getCrcbHalf() {
        return crcbHalf;
    }
}
