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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

import static com.sternkn.djvu.file.coders.BSByteStreamUtils.CTXIDS;
import static com.sternkn.djvu.file.coders.BSByteStreamUtils.KILOBYTE;
import static com.sternkn.djvu.file.coders.BSByteStreamUtils.MAX_BLOCK;
import static com.sternkn.djvu.file.coders.BSByteStreamUtils.getXMTF;

public class BSByteOutputStream extends OutputStream {
    private static final Logger LOG = LoggerFactory.getLogger(BSByteOutputStream.class);

    private static final int MIN_BLOCK = 10;
    private static final int FREQS0 = 100000;
    private static final int FREQS1 = 1000000;
    private static final int FREQMAX = 4;

    // Overflow required when encoding
    private static final int OVERFLOW = 32;

    private final ZPCodecEncoder zpEncoder;
    private final int blocksize;

    private int[] gdata;
    private int bptr;
    private int size;

    private BitContext[] ctx;

    public BSByteOutputStream(OutputStream outputStream, int xencoding) {
        zpEncoder = new ZpCodecOutputStream(outputStream);

        int encoding = Math.max(xencoding, MIN_BLOCK);
        if (encoding > MAX_BLOCK) {
            throw new DjVuFileException("The block size is greater than " + MAX_BLOCK);
        }

        blocksize = encoding * KILOBYTE;
        LOG.debug("blocksize = {}", blocksize);

        this.ctx = new BitContext[300];

        for (int ind = 0; ind < this.ctx.length; ind++) {
            ctx[ind] = new BitContext();
        }
    }

    @Override
    public void write(int b) throws IOException {
        write(new int[]{b});
    }

    @Override
    public void write(byte[] buffer) throws IOException {
        if (buffer == null || buffer.length == 0) {
            return;
        }

        int[] buff = new int[buffer.length];
        for (int i = 0; i < buffer.length; i++) {
            buff[i] = buffer[i] & 0xFF;
        }

        write(buff);
    }

    private void write(int[] buffer) throws IOException {
        int sz = buffer.length;

        while (sz > 0) {
            if (gdata == null) {
                this.bptr = 0;
                this.gdata = new int[blocksize + OVERFLOW];
            }

            int bytes = Math.min(blocksize - 1 - bptr, sz);
            System.arraycopy(buffer, 0, gdata, bptr, bytes);

            bptr += bytes;
            sz -= bytes;

            // Flush when needed
            if (bptr + 1 >= blocksize) {
                LOG.debug("Flushing during writing");
                flush();
            }
        }
    }

    @Override
    public void flush() throws IOException {
        if (bptr > 0) {
            if (bptr >= blocksize) {
                throw new DjVuFileException("The bptr (" + bptr + ") is greater than blocksize(" + blocksize + ")");
            }

            size = bptr + 1;
            encode();
        }
        size = 0;
        bptr = 0;
    }

    @Override
    public void close() throws IOException {
        flush();

        // Encode EOF marker
        encodeRaw(24, 0);

        zpEncoder.close();
    }

    private long encode() {
        final int markerpos = blocksort(gdata, size);
        LOG.debug("encode: markerpos = {}", markerpos);

        encodeRaw(24, size);

        // Determine and Encode Estimation Speed
        int fshift = 0;
        if (size < FREQS0) {
            zpEncoder.encoder(0);
        }
        else if (size < FREQS1) {
            fshift = 1;
            zpEncoder.encoder(1);
            zpEncoder.encoder(0);
        }
        else {
            fshift = 2;
            zpEncoder.encoder(1);
            zpEncoder.encoder(1);
        }

        FrequenciesContext frequenciesContext = new FrequenciesContext(fshift);

        for (int i = 0; i < size; i++)
        {
            // Get MTF data
            int c = gdata[i];
            int ctxid = Math.min(CTXIDS - 1, frequenciesContext.mtfno);

            frequenciesContext.mtfno = frequenciesContext.rmtf[c];
            if (i == markerpos) {
                frequenciesContext.mtfno = 256;
            }

            ArrayPointer<BitContext> cx = new ArrayPointer<>(ctx);
            int b = (frequenciesContext.mtfno == 0) ? 1 : 0;
            zpEncoder.encoder(b, cx.getValue(ctxid));
            if (b == 1) {
                frequenciesContext.adjustFrequenciesForOverflow(c);
                continue;
            }
            cx.shift(CTXIDS);
            b = (frequenciesContext.mtfno == 1) ? 1 : 0;
            zpEncoder.encoder(b, cx.getValue(ctxid));
            if (b == 1) {
                frequenciesContext.adjustFrequenciesForOverflow(c);
                continue;
            }
            cx.shift(CTXIDS);
            b = (frequenciesContext.mtfno < 4) ? 1 : 0;
            zpEncoder.encoder(b, cx.getCurrentValue());
            if (b == 1) {
                encodeBinary(cx, 1, frequenciesContext.mtfno - 2);
                frequenciesContext.adjustFrequenciesForOverflow(c);
                continue;
            }
            cx.shift(2);
            b = (frequenciesContext.mtfno < 8) ? 1 : 0;
            zpEncoder.encoder(b, cx.getCurrentValue());
            if (b == 1) {
                encodeBinary(cx,2,frequenciesContext.mtfno - 4);
                frequenciesContext.adjustFrequenciesForOverflow(c);
                continue;
            }
            cx.shift(4);
            b = (frequenciesContext.mtfno < 16) ? 1 : 0;
            zpEncoder.encoder(b, cx.getCurrentValue());
            if (b == 1) {
                encodeBinary(cx,3,frequenciesContext.mtfno - 8);
                frequenciesContext.adjustFrequenciesForOverflow(c);
                continue;
            }
            cx.shift(8);
            b = (frequenciesContext.mtfno < 32) ? 1 : 0;
            zpEncoder.encoder(b, cx.getCurrentValue());
            if (b == 1) {
                encodeBinary(cx,4,frequenciesContext.mtfno - 16);
                frequenciesContext.adjustFrequenciesForOverflow(c);
                continue;
            }
            cx.shift(16);
            b = (frequenciesContext.mtfno < 64) ? 1 : 0;
            zpEncoder.encoder(b, cx.getCurrentValue());
            if (b == 1) {
                encodeBinary(cx,5,frequenciesContext.mtfno - 32);
                frequenciesContext.adjustFrequenciesForOverflow(c);
                continue;
            }
            cx.shift(32);
            b = (frequenciesContext.mtfno < 128) ? 1 : 0;
            zpEncoder.encoder(b, cx.getCurrentValue());
            if (b == 1) {
                encodeBinary(cx,6,frequenciesContext.mtfno - 64);
                frequenciesContext.adjustFrequenciesForOverflow(c);
                continue;
            }
            cx.shift(64);
            b = (frequenciesContext.mtfno < 256) ? 1 : 0;
            zpEncoder.encoder(b, cx.getCurrentValue());
            if (b == 1) {
                encodeBinary(cx,7,frequenciesContext.mtfno - 128);
                frequenciesContext.adjustFrequenciesForOverflow(c);
            }
        }

        return 0;
    }

    private int blocksort(int[] data, int size) {
        BSort bsort = new BSort(data, size);
        return bsort.run();
    }

    private static class FrequenciesContext {
        int[] mtf;
        int[] rmtf;
        long[] freq;
        int fadd;
        int mtfno;
        int fshift;

        FrequenciesContext(int fshift) {
            this.fshift = fshift;
            mtf = getXMTF();
            rmtf = getXMTF();
            freq = new long[FREQMAX];
            fadd = 4;
            mtfno = 3;
        }

        private void adjustFrequenciesForOverflow(int c) {
            fadd = fadd + (fadd >> fshift);
            if (fadd > 0x10000000) {
                fadd = fadd >> 24;
                for (int k = 0; k < FREQMAX; k++) {
                    freq[k] = freq[k] >> 24;
                }
            }

            // Relocate new char according to new freq
            long fc = fadd;
            if (mtfno < FREQMAX) {
                fc += freq[mtfno];
            }
            int k;
            for (k = mtfno; k >= FREQMAX; k--) {
                mtf[k] = mtf[k-1];
                rmtf[mtf[k]] = k;
            }
            for (; k > 0 && fc >= freq[k-1]; k--) {
                mtf[k] = mtf[k-1];
                freq[k] = freq[k-1];
                rmtf[mtf[k]] = k;
            }
            mtf[k] = c;
            freq[k] = fc;
            rmtf[mtf[k]] = k;
        }
    }

    private void encodeBinary(ArrayPointer<BitContext> ctx, int bits, int x) {
        int n = 1;
        int m = (1 << bits);

        while (n < m) {
            x = (x & (m - 1)) << 1;
            int b = (x >> bits);
            zpEncoder.encoder(b, ctx.getValue(n));
            n = (n << 1) | b;
        }
    }

    private void encodeRaw(int bits, int x) {
        int n = 1;
        int m = (1 << bits);
        while (n < m) {
            x = (x & (m - 1)) << 1;
            int b = (x >> bits);
            zpEncoder.encoder(b);
            n = (n << 1) | b;
        }
    }
}
