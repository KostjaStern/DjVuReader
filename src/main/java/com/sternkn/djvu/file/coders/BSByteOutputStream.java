package com.sternkn.djvu.file.coders;

import com.sternkn.djvu.file.DjVuFileException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.stream.IntStream;

public class BSByteOutputStream extends OutputStream {
    private static final int MINBLOCK = 10;
    private static final int MAXBLOCK = 4096;
    private static final int FREQS0 = 100000;
    private static final int FREQS1 = 1000000;
    private static final int FREQMAX = 4;
    private static final int CTXIDS = 3;

    private static final int[] XMTF = IntStream.range(0, 256).toArray();

    // Overflow required when encoding
    private static final int OVERFLOW = 32;


    private final ZPCodecEncoder zpEncoder;
    private final int blocksize;

    private int[] gdata;
    // private int offset;
    private int bptr;
    private int size;

    private BitContext[] ctx;

    public BSByteOutputStream(OutputStream outputStream, int xencoding) {
        zpEncoder = new ZpCodecOutputStream(outputStream);
        // gzp=ZPCodec::create(gbs,true,true);

        int encoding = Math.max(xencoding, MINBLOCK);
        if (encoding > MAXBLOCK) {
            throw new DjVuFileException("The block size is greater than " + MAXBLOCK);
        }

        blocksize = encoding * 1024;
        // this.offset = 0;

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
        // int copied = 0;

        while (sz > 0) {
            if (gdata == null) {
                this.bptr = 0;
                this.gdata = new int[blocksize + OVERFLOW];
            }

            int bytes = Math.min(blocksize - 1 - bptr, sz);
//            if (bytes > sz) {
//                bytes = sz;
//            }

            // Store date (todo: rle)
            // memcpy(data+bptr, buffer, bytes);
            // buffer = (void*)((char*)buffer + bytes);
            System.arraycopy(buffer, 0, gdata, bptr, bytes);

            bptr += bytes;
            sz -= bytes;
            // copied += bytes;
            // offset += bytes;

            // Flush when needed
            if (bptr + 1 >= blocksize) {
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
            // ASSERT(bptr<(int)blocksize);
            // memset(data+bptr, 0, OVERFLOW);
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
        encode_raw(24, 0);

        zpEncoder.close();
    }

    private long encode() {
        final int markerpos = blocksort(gdata, size);

        encode_raw(24, size);

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
                encode_binary(cx, 1, frequenciesContext.mtfno - 2);
                frequenciesContext.adjustFrequenciesForOverflow(c);
                continue;
            }
            cx.shift(2);
            b = (frequenciesContext.mtfno < 8) ? 1 : 0;
            zpEncoder.encoder(b, cx.getCurrentValue());
            if (b == 1) {
                encode_binary(cx,2,frequenciesContext.mtfno - 4);
                frequenciesContext.adjustFrequenciesForOverflow(c);
                continue;
            }
            cx.shift(4);
            b = (frequenciesContext.mtfno < 16) ? 1 : 0;
            zpEncoder.encoder(b, cx.getCurrentValue());
            if (b == 1) {
                encode_binary(cx,3,frequenciesContext.mtfno - 8);
                frequenciesContext.adjustFrequenciesForOverflow(c);
                continue;
            }
            cx.shift(8);
            b = (frequenciesContext.mtfno < 32) ? 1 : 0;
            zpEncoder.encoder(b, cx.getCurrentValue());
            if (b == 1) {
                encode_binary(cx,4,frequenciesContext.mtfno - 16);
                frequenciesContext.adjustFrequenciesForOverflow(c);
                continue;
            }
            cx.shift(16);
            b = (frequenciesContext.mtfno < 64) ? 1 : 0;
            zpEncoder.encoder(b, cx.getCurrentValue());
            if (b == 1) {
                encode_binary(cx,5,frequenciesContext.mtfno - 32);
                frequenciesContext.adjustFrequenciesForOverflow(c);
                continue;
            }
            cx.shift(32);
            b = (frequenciesContext.mtfno < 128) ? 1 : 0;
            zpEncoder.encoder(b, cx.getCurrentValue());
            if (b == 1) {
                encode_binary(cx,6,frequenciesContext.mtfno - 64);
                frequenciesContext.adjustFrequenciesForOverflow(c);
                continue;
            }
            cx.shift(64);
            b = (frequenciesContext.mtfno < 256) ? 1 : 0;
            zpEncoder.encoder(b, cx.getCurrentValue());
            if (b == 1) {
                encode_binary(cx,7,frequenciesContext.mtfno - 128);
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
            mtf = Arrays.copyOf(XMTF, XMTF.length);
            rmtf = Arrays.copyOf(XMTF, XMTF.length);
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


    private void encode_binary(ArrayPointer<BitContext> ctx, int bits, int x) {
        int n = 1;
        int m = (1 << bits);

        while (n < m)
        {
            x = (x & (m-1)) << 1;
            int b = (x >> bits);
            zpEncoder.encoder(b, ctx.getValue(n));
            n = (n<<1) | b;
        }
    }

    private void encode_raw(int bits, int x) {
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
