package com.sternkn.djvu.file.coders;


import com.sternkn.djvu.file.DjVuFileException;

import java.util.ArrayList;
import java.util.List;

import static com.sternkn.djvu.file.utils.NumberUtils.asUnsignedInt;

/*
    Represents all the blocks of an image
 */
public class IW44ImageMap {

    private static final int IWALLOCSIZE = 4080;
    private static final int IW_SHIFT = 6;
    private static final int IW_ROUND = (1 << (IW_SHIFT - 1));

    // geometry
    int iw;
    int ih;
    private int bw;
    private int bh;
    int nb;
    private int top;
    private IW44ImageBlock[] blocks;

    private List<int[]> chain;

    public IW44ImageMap(int w, int h) {
        this.iw = w;
        this.ih = h;

        this.bw = (w + 0x20 - 1) & ~0x1f;
        this.bh = (h + 0x20 - 1) & ~0x1f;
        this.nb = (int) (asUnsignedInt((long) bw * bh) / (32 * 32));

        blocks = new IW44ImageBlock[nb];
        for (int i = 0; i < nb; i++) {
            blocks[i] = new IW44ImageBlock();
        }

        top = IWALLOCSIZE;
        chain = new ArrayList<>();
    }

    public IW44ImageBlock getBlock(int ind) {
        if (ind < 0 || ind >= nb) {
            throw new DjVuFileException("Invalid block index: " + ind);
        }
        return blocks[ind];
    }

    public BufferPointer alloc(int n) {
        if (top + n > IWALLOCSIZE)
        {
            // note: everything is cleared long before we use it
            // in order to avoid the need for a memory fence.
            // chain = new IW44Image::Alloc(chain);
            chain.add(new int[IWALLOCSIZE]);
            top = 0;
        }
        // short *ans = chain->data + top;
        BufferPointer buffer = new BufferPointer(chain.getLast(), top);
        top += n;
        return buffer;
    }

    public void image(GPixmap ppm, ColorName colorName, int fast) {
        // Allocate reconstruction buffer
        int[] data16;
        int sz = bw * bh;
        if ((sz / bw) != bh) { // multiplication overflow
            throw new DjVuFileException("IW44Image: image size exceeds maximum (corrupted file?)");
        }

        data16 = new int[sz];

        // Copy coefficients
        int i;
        BufferPointer p = new BufferPointer(data16);
        int block_ind = 0;
        IW44ImageBlock block = null;
        for (i = 0; i < bh; i += 32)
        {
            for (int j = 0; j < bw; j += 32)
            {
                block = blocks[block_ind];
                int[] liftblock = block.write_liftblock(0, 64);

                block_ind++;

                BufferPointer pp = p.shiftPointer(j);
                BufferPointer pl = new BufferPointer(liftblock);
                for (int ii = 0; ii < 32; ii++, pp = pp.shiftPointer(bw), pl = pl.shiftPointer(32)) {
                    // memcpy(( void*)pp, ( void*)pl, 32 * sizeof( short));
                    for (int ind = 0; ind < 32; ind++) {
                        pp.setValue(ind, pl.getValue(ind));
                    }
                }
            }

            // next row of blocks
            p = p.shiftPointer(32 * bw);
        }

        // Reconstruction
        if (fast != 0)
        {
            backward(data16, iw, ih, bw, 32, 2);
            p = new BufferPointer(data16);
            for (i = 0; i < bh; i += 2, p = p.shiftPointer(bw)) {
                for (int jj = 0; jj < bw; jj += 2, p = p.shiftPointer(2)) {
                    p.setValue(1, p.getCurrentValue());
                    p.setValue(bw + 1, p.getValue(1));
                    p.setValue(bw, p.getValue(bw + 1));
                }
            }
        }
        else
        {
            backward(data16, iw, ih, bw, 32, 1);
        }

        // Copy result into image
        p = new BufferPointer(data16);
        ArrayPointer<PixelColor> row = new ArrayPointer<>(ppm.getPixels());
        for (i = 0; i < ih; i++)
        {
            ArrayPointer<PixelColor> pix = row;
            for (int j = 0; j < iw; j += 1, pix = pix.shiftPointer(1)) // pixsep
            {
                int x = (p.getValue(j) + IW_ROUND) >> IW_SHIFT;
                if (x < -128) {
                    x = -128;
                } else if (x > 127) {
                    x = 127;
                }

                PixelColor pixel = pix.getCurrentValue();
                if (pixel == null) {
                    pixel = new PixelColor();
                    pix.setValue(0, pixel);
                }
                pixel.setColor(colorName, x);
            }
            row.shift(ppm.getColumns());
            p.shift(bw);
        }
    }

    void backward(int[] p, int w, int h, int rowsize, int begin, int end) {
        for (int scale = begin >> 1; scale >= end; scale >>= 1) {
            filter_bv(p, w, h, rowsize, scale);
            filter_bh(p, w, h, rowsize, scale);
        }
    }

    static void filter_bv(int[] p, int w, int h, int rowsize, int scale) {
        int y = 0;
        int s = scale * rowsize;
        int s3 = s + s + s;
        h = ((h - 1) / scale) + 1;
        BufferPointer pp = new BufferPointer(p);

        while (y-3 < h)
        {
            // 1-Lifting
            {
                BufferPointer q = new BufferPointer(pp);
                BufferPointer e = q.shiftPointer(w);
                if (y >= 3 && y + 3 < h)
                {
                    // Generic case
                    while (q.isPointerLess(e)) {
                        int a = q.getValue(-s) + q.getValue(s);
                        int b = q.getValue(-s3) + q.getValue(s3);
                        int qShift = (((a << 3) + a - b + 16) >> 5);
                        q.setCurrentValue(q.getCurrentValue() - qShift);
                        q = q.shiftPointer(scale);
                    }
                }
                else if (y < h)
                {
                    // Special cases
                    BufferPointer q1 = (y + 1 < h ? q.shiftPointer(s) : null);
                    BufferPointer q3 = (y + 3 < h ? q.shiftPointer(s3) : null);
                    if (y >= 3)
                    {
                        while (q.isPointerLess(e)) {
                            int a = q.getValue(-s) + (q1 != null ? q1.getCurrentValue() : 0);
                            int b = q.getValue(-s3) + (q3 != null ? q3.getCurrentValue() : 0);
                            int qShift = (((a << 3) + a - b + 16) >> 5);
                            q.setCurrentValue(q.getCurrentValue() - qShift);
                            q = q.shiftPointer(scale);

                            if (q1 != null) {
                                q1 = q1.shiftPointer(scale);
                            }
                            if (q3 != null) {
                                q3 = q3.shiftPointer(scale);
                            }
                        }
                    }
                    else if (y >= 1)
                    {
                        while (q.isPointerLess(e))
                        {
                            int a = q.getValue(-s) + (q1 != null ? q1.getCurrentValue() : 0);
                            int b = (q3 != null ? q3.getCurrentValue() : 0);
                            int qShift = (((a << 3) + a - b + 16) >> 5);

                            q.setCurrentValue(q.getCurrentValue() - qShift);
                            q = q.shiftPointer(scale);

                            if (q1 != null) {
                                q1 = q1.shiftPointer(scale);
                            }
                            if (q3 != null) {
                                q3 = q3.shiftPointer(scale);
                            }
                        }
                    }
                    else
                    {
                        while (q.isPointerLess(e)) {
                            int a = (q1 != null ? q1.getCurrentValue() : 0);
                            int b = (q3 != null ? q3.getCurrentValue() : 0);
                            int qShift = (((a << 3) + a - b + 16) >> 5);

                            q.setCurrentValue(q.getCurrentValue() - qShift);
                            q = q.shiftPointer(scale);

                            if (q1 != null) {
                                q1 = q1.shiftPointer(scale);
                            }
                            if (q3 != null) {
                                q3 = q3.shiftPointer(scale);
                            }
                        }
                    }
                }
            }
            // 2-Interpolation
            {
                BufferPointer q = pp.shiftPointer(-s3);
                BufferPointer e = q.shiftPointer(w);
                if (y >= 6 && y < h)
                {
                    while (q.isPointerLess(e)) {
                        int a = q.getValue(-s) + q.getValue(s);
                        int b = q.getValue(-s3) + q.getValue(s3);
                        int qShift = (((a << 3) + a - b + 8) >> 4);

                        q.setCurrentValue(q.getCurrentValue() + qShift);
                        q = q.shiftPointer(scale);
                    }
                }
                else if (y >= 3)
                {
                    // Special cases
                    BufferPointer q1 = (y - 2 < h ? q.shiftPointer(s) : q.shiftPointer(-s));
                    while (q.isPointerLess(e)) {
                        int a = q.getValue(-s) + q1.getCurrentValue();
                        q.setCurrentValue(q.getCurrentValue() + ((a + 1) >> 1));
                        q = q.shiftPointer(scale);
                        q1 = q1.shiftPointer(scale);
                    }
                }
            }
            y += 2;
            pp.shift(s + s);
        }
    }

    static void filter_bh(int[] p, int w, int h, int rowsize, int scale) {
        int y = 0;
        int s = scale;
        int s3 = s + s + s;
        rowsize *= scale;
        BufferPointer pp = new BufferPointer(p);
        while (y < h)
        {
            BufferPointer q = new BufferPointer(pp);
            BufferPointer e = pp.shiftPointer(w);
            int a0 = 0;
            int a1 = 0;
            int a2 = 0;
            int a3 = 0;
            int b0 = 0;
            int b1 = 0;
            int b2 = 0;
            int b3 = 0;
            if (q.isPointerLess(e))
            {
                // Special case:  x=0
                if (q.shiftPointer(s).isPointerLess(e)) {
                    a2 = q.getValue(s);
                }
                if (q.shiftPointer(s3).isPointerLess(e)) {
                    a3 = q.getValue(s3);
                }
                int shift = ((((a1 + a2) << 3) + (a1 + a2) - a0 - a3 + 16) >> 5);
                b2 = q.getCurrentValue() - shift;
                b3 = q.getCurrentValue() - shift;

                q.setCurrentValue(b3);
                q.shift(s + s);
            }
            if (q.isPointerLess(e))
            {
                // Special case:  x=2
                a0 = a1;
                a1 = a2;
                a2 = a3;
                if (q.shiftPointer(s3).isPointerLess(e)) {
                    a3 = q.getValue(s3);
                }
                int shift = ((((a1 + a2) << 3) + (a1 + a2) - a0 - a3 + 16) >> 5);
                b3 = q.getCurrentValue() - shift;
                q.setCurrentValue(b3);
                q.shift(s + s);
            }
            if (q.isPointerLess(e))
            {
                // Special case:  x=4
                b1 = b2;
                b2 = b3;
                a0 = a1;
                a1 = a2;
                a2 = a3;
                if (q.shiftPointer(s3).isPointerLess(e)) {
                    a3 = q.getValue(s3);
                }
                int shift = ((((a1 + a2) << 3) + (a1 + a2) - a0 - a3 + 16) >> 5);
                b3 = q.getCurrentValue() - shift;
                q.setValue(0, b3);

                q.setValue(-s3, q.getValue(-s3) + ((b1 + b2 + 1) >> 1));
                q.shift(s + s);
            }
            while (q.shiftPointer(s3).isPointerLess(e))
            {
                // Generic case
                a0 = a1;
                a1 = a2;
                a2 = a3;
                a3 = q.getValue(s3);
                b0 = b1;
                b1 = b2;
                b2 = b3;

                int shift1 = ((((a1 + a2) << 3) + (a1 + a2) - a0 - a3 + 16) >> 5);
                b3 = q.getCurrentValue() - shift1;
                q.setCurrentValue(b3);

                int shift2 = ((((b1 + b2) << 3) + (b1 + b2) - b0 - b3 + 8) >> 4);
                q.setValue(-s3, q.getValue(-s3) + shift2);
                q.shift(s + s);
            }
            while (q.isPointerLess(e))
            {
                // Special case:  w-3 <= x < w
                a0 = a1;
                a1 = a2;
                a2 = a3;
                a3 = 0;
                b0 = b1;
                b1 = b2;
                b2 = b3;

                int shift1 = ((((a1 + a2) << 3) + (a1 + a2) - a0 - a3 + 16) >> 5);
                b3 = q.getCurrentValue() - shift1;
                q.setCurrentValue(b3);

                int shift2 = ((((b1 + b2) << 3) + (b1 + b2) - b0 - b3 + 8) >> 4);
                q.setValue(-s3, q.getValue(-s3) + shift2);
                q.shift(s + s);
            }
            while (q.shiftPointer(-s3).isPointerLess(e))
            {
                // Special case  w <= x < w+3
                b0 = b1;
                b1 = b2;
                b2 = b3;
                if (!(q.shiftPointer(-s3).isPointerLess(pp))) {
                    q.setValue(-s3, q.getValue(-s3) + ((b1 + b2 + 1) >> 1));
                }
                q.shift(s + s);
            }
            y += scale;
            pp.shift(rowsize);
        }
    }
}
