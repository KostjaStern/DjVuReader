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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.sternkn.djvu.utils.InputStreamUtils.read8;

public class IW44Image {

    private int cslice;
    private int cserial;

    private int crcb_delay;
    private int crcb_half;

    private InputStream inputStream;

    private IW44ImageDecoder ycodec;
    private IW44ImageDecoder cbcodec;
    private IW44ImageDecoder crcodec;

    private IW44ImageMap ymap;
    private IW44ImageMap cbmap;
    private IW44ImageMap crmap;

    private ZPCodecDecoder zpDecoder;

    private IW44SecondaryHeader secondaryHeader;

    public IW44Image() {
    }

    public int getWidth() {
        return secondaryHeader != null ? secondaryHeader.getWidth() : 0;
    }

    public int getHeight() {
        return secondaryHeader != null ? secondaryHeader.getHeight() : 0;
    }

    public IW44SecondaryHeader getSecondaryHeader() {
        return secondaryHeader;
    }

    public GPixmap get_pixmap() {
        // Check presence of data
        if (ymap == null) {
            return null;
        }

        // Allocate pixmap
        int w = ymap.iw;
        int h = ymap.ih;
        GPixmap ppm = new GPixmap(h, w);

        // Perform wavelet reconstruction
        ymap.image(ppm, ColorName.BLUE, 0);

        if (crmap != null && cbmap != null && crcb_delay >= 0)
        {
            cbmap.image(ppm, ColorName.GREEN, crcb_half);
            crmap.image(ppm, ColorName.RED, crcb_half);
        }

        // Convert image data to RGB
        if (crmap != null && cbmap != null && crcb_delay >= 0)
        {
            YCbCr_to_RGB(ppm);
        }
//        else
//        {
//            for (int i = 0; i < h; i++)
//            {
//                // GPixel *pixrow = (*ppm)[i];
//                ArrayPointer<PixelColor> pixrow = new ArrayPointer<>(ppm.getPixels(), i * ppm.getColumns());
//                for (int j = 0; j < w; j++, pixrow++) {
//                    pixrow -> b = pixrow -> g = pixrow -> r
//                            = 127 - (int) (((signed char*)pixrow)[0]);
//                }
//            }
//        }

        return ppm;
    }

    /* Converts YCbCr to RGB. */
    void YCbCr_to_RGB(GPixmap ppm) {
        for (PixelColor pixelColor : ppm.getPixels()) {
            YCbCr_to_RGB(pixelColor);
        }
    }

    void YCbCr_to_RGB(PixelColor pixelColor) {
        int y = pixelColor.getBlue();
        int b = pixelColor.getGreen();
        int r = pixelColor.getRed();

        // This is the Pigeon transform
        int t1 = b >> 2 ;
        int t2 = r + (r >> 1);
        int t3 = y + 128 - t1;
        int tr = y + 128 + t2;
        int tg = t3 - (t2 >> 1);
        int tb = t3 + (b << 1);

        pixelColor.setColor(ColorName.RED, Math.max(0, Math.min(255, tr)));
        pixelColor.setColor(ColorName.GREEN, Math.max(0, Math.min(255, tg)));
        pixelColor.setColor(ColorName.BLUE, Math.max(0, Math.min(255, tb)));
    }


    public void close_codec() {
        ycodec = null;
        cbcodec = null;
        crcodec = null;
    }

    public int decode_chunk(byte[] data) {
        this.inputStream = new ByteArrayInputStream(data);

        // Open
        if (ycodec == null) {
            cslice = 0;
            cserial = 0;
        }

        PrimaryHeader primary =  new PrimaryHeader();
        primary.decode(inputStream);
        if (primary.serial != cserial) {
            throw new DjVuFileException("IW44Image.wrong_serial2");
        }

        int nslices = cslice + primary.slices;

        // Read secondary header
        if (cserial == 0) {
            secondaryHeader = new IW44SecondaryHeader(inputStream);
            int w = secondaryHeader.getWidth();
            int h = secondaryHeader.getHeight();
            crcb_delay = secondaryHeader.getChrominanceDelay();
            crcb_half = secondaryHeader.getCrcbHalf();

            ymap = new IW44ImageMap(w, h);
            ycodec = new IW44ImageDecoder(ymap);
            if (crcb_delay >= 0) {
                cbmap = new IW44ImageMap(w, h);
                crmap = new IW44ImageMap(w, h);
                cbcodec = new IW44ImageDecoder(cbmap);
                crcodec = new IW44ImageDecoder(crmap);
            }
        }

        this.zpDecoder = new ZpCodecInputStream(inputStream);
        int flag = 1;

        while (flag != 0 && cslice < nslices)
        {
            flag = ycodec.code_slice(zpDecoder);
            if (crcodec != null && cbcodec != null && crcb_delay <= cslice) {
                flag |= cbcodec.code_slice(zpDecoder);
                flag |= crcodec.code_slice(zpDecoder);
            }
            cslice++;
        }
        // Return
        cserial += 1;
        return nslices;
    }


    public static class PrimaryHeader {
        /*
            Serial number. A one-octet unsigned integer. The serial number of the first chunk of a
            given chunk type is 0. Successive chunks are assigned consecutive serial numbers.
         */
        int serial;

        /*
            Number of slices. A one-octet unsigned integer. The number of slices coded in the chunk.
         */
        int slices;

        void decode(InputStream inputStream) {
            serial = read8(inputStream);
            slices = read8(inputStream);
        }
    }
}
