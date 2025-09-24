package com.sternkn.djvu.file.coders;

import com.sternkn.djvu.file.DjVuFileException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.sternkn.djvu.file.utils.InputStreamUtils.read8;

public class IW44Image {
    private static final int IWCODEC_MAJOR = 1;
    private static final int IWCODEC_MINOR = 2;

    private int cslice;
    private int cserial;
    private int cbytes;

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

    public IW44Image() {

    }

    public int getWidth() {
        return ymap != null ? ymap.iw : 0;
    }

    public int getHeight() {
        return ymap != null ? ymap.ih : 0;
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
        cslice = 0;
        cbytes = 0;
        cserial = 0;
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
            SecondaryHeader secondary = new SecondaryHeader();
            secondary.decode(inputStream);
            if ((secondary.major & 0x7f) != IWCODEC_MAJOR) {
                throw new DjVuFileException("IW44Image.incompat_codec");
            }
            if (secondary.minor > IWCODEC_MINOR) {
                throw new DjVuFileException("IW44Image.recent_codec");
            }

            TertiaryHeader tertiary = new TertiaryHeader();
            tertiary.decode(inputStream, secondary.major & 0x7f, secondary.minor);

            // Handle header information
            int w = (tertiary.xhi << 8) | tertiary.xlo;
            int h = (tertiary.yhi << 8) | tertiary.ylo;
            crcb_delay = 0;
            crcb_half = 0;
            if (secondary.minor >= 2) {
                crcb_delay = tertiary.crcbdelay & 0x7f;
            }
            if (secondary.minor >= 2) {
                crcb_half = (((tertiary.crcbdelay & 0x80) != 0) ? 0 : 1);
            }
            if ((secondary.major & 0x80) != 0) {
                crcb_delay = -1;
            }

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
        int serial;
        int slices;

        void decode(InputStream inputStream) {
            serial = read8(inputStream);
            slices = read8(inputStream);
        }
    }

    public static class SecondaryHeader {
        int major;
        int minor;

        void decode(InputStream inputStream) {
            major = read8(inputStream);
            minor = read8(inputStream);
        }
    }

    public static class TertiaryHeader {
        int xhi;
        int xlo;
        int yhi;
        int ylo;
        int crcbdelay;

        // tertiary.decode(gbs, secondary.major & 0x7f, secondary.minor);
        void decode(InputStream inputStream, int major, int minor) {
            xhi = read8(inputStream);
            xlo = read8(inputStream);
            yhi = read8(inputStream);
            ylo = read8(inputStream);
            crcbdelay = 0;

            if (major== 1 && minor >= 2) {
                crcbdelay = read8(inputStream);
            }
        }
    }
}
