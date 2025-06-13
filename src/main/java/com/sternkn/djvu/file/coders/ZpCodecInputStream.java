package com.sternkn.djvu.file.coders;

import com.sternkn.djvu.file.DjVuFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import static com.sternkn.djvu.file.utils.NumberUtils.asUnsignedInt;
import static com.sternkn.djvu.file.utils.NumberUtils.asUnsignedShort;

/*
    12 Appendix 3: Z´coding.
    The Z´-Coder is an approximate binary arithmetic coder.

    https://sourceforge.net/p/djvu/djvulibre-git/ci/master/tree/libdjvu/ZPCodec.h
    https://sourceforge.net/p/djvu/djvulibre-git/ci/master/tree/libdjvu/ZPCodec.cpp
 */
public class ZpCodecInputStream implements ZPCodecDecoder, Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(ZpCodecInputStream.class);

    private static final int NO_MORE_BYTE = -1;

    private final InputStream inputStream;

    private final byte[] ffzt;
    private final ZpCodecTable[] table;

    private byte delay;
    private int scount;

    private int currentByte;

    private long fence;
    private long buffer;
    private long a;
    private long code;

    /**
     * At the beginning of a chunk, the values of {a} and {code} are reinitialized. When the decoder is
     * decoding a chunk, it may require more bits than are present within the chunk's data. In
     * this case, all additional required bits are to be assumed by the decoder to be 1. If there are
     * excess bits at the end of a chunk, they are ignored.
     */
    public ZpCodecInputStream(InputStream inputStream) {
        this.inputStream = inputStream;

        this.ffzt = ZpCodecUtils.getFFZTable();
        this.table = ZpCodecUtils.getDefaultTable();

        this.fence = 0;
        this.buffer = 0;

        init();
    }

    @Override
    public void close() throws IOException {
        this.inputStream.close();
    }

    /*
       Initially, A is set to 0x0000. Two octets are read from the input data stream into the
       lowest 16 bits of C. If the bits of C are numbered such that bit 15 is the most significant
       bit and bit 0 is the least significant bit, then the first input octet is stored in bits 15
       through 8, and the second input octet is stored in bits 7 through 0.

       this.a    <-> A
       this.code <-> C
     */
    private void init() {
        this.a = 0;

        readNextByte();
        this.code = ((long) this.currentByte << 8);

        readNextByte();
        this.code = this.code | this.currentByte;

        /* Preload buffer */
        this.delay = 25;
        this.scount = 0;
        preload();

        /* Compute initial fence */
        this.fence = Math.min(this.code, 0x7fff);
    }

    private int readNextByte() {
        int value;
        try {
            value = this.inputStream.read();
        } catch (IOException e) {
            throw new DjVuFileException("We can not read next byte", e);
        }

        this.currentByte = value == NO_MORE_BYTE ? 0xFF : value;
        return value;
    }

    private void preload() {
        while (this.scount <= 24) {
            if (readNextByte() == NO_MORE_BYTE) {
                --this.delay;

                LOG.debug("preload: delay = {}", this.delay);

                if (this.delay < 1) {
                    throw new DjVuFileException("End of djvu file");
                }
            }

            this.buffer = asUnsignedInt((this.buffer << 8) | this.currentByte);
            this.scount += 8;
        }
    }

    /**
     *  In pass-through mode, the decoder is invoked with no input argument. No context is
     *  involved.
     *
     *  B is the 1-bit value returned by the decoder.
     */
    @Override
    public int decoder() {
        return decodeSubSimple(0, 0x8000 + (this.a >> 1));
    }

    /**
     *
     */
    @Override
    public int decoder(BitContext ctx) {
        final int index = ctx.getValue();

        if (index < 0 || index >= this.table.length ) {
            throw new IllegalArgumentException("The index should be in range 0 .. " + (this.table.length - 1));
        }

        long z = this.a + this.table[index].p();
        if (z <= this.fence) {
            this.a = z;
            return (index & 1);
        }

        return decodeSub(ctx, z);
    }

    private int ffz(long x) {
        final int lastByte = (int) (x & 0xff);
        final int penultimateByte = (int) ((x >> 8) & 0xff);
        return (x >= 0xff00) ? (this.ffzt[lastByte] + 8) : (this.ffzt[penultimateByte]);
    }

    /**
     *  see decode_sub from ZPCodec.cpp
     */
    private int decodeSub(BitContext ctx, long z) {
        long zz = z;
        int index = ctx.getValue();
        int bit = (index & 1);

        /* Avoid interval reversion (#ifdef ZPCODER) */
        long d = asUnsignedInt(0x6000 + ((zz + this.a) >> 2));
        if (zz > d) {
            zz = d;
        }

        /* Test MPS/LPS */
        if (zz > this.code)
        {
            /* LPS branch */
            zz = 0x10000 - zz;
            this.a = this.a + zz;
            this.code = this.code + zz;

            /* LPS adaptation */
            ctx.setValue(this.table[index].dn());

            /* LPS renormalization */
            lpsRenormalization();

            adjustFence();

            return bit ^ 1;
        }
        else
        {
            /* MPS adaptation */
            if (this.a >= this.table[index].m()) {
                ctx.setValue(this.table[index].up());
            }

            /* MPS renormalization */
            this.scount -= 1;
            this.a = asUnsignedShort(zz << 1);
            this.code = asUnsignedShort((this.code << 1) | ((this.buffer >> this.scount) & 1));

            adjustFence();

            return bit;
        }
    }

    /**
     *  see decode_sub_simple from ZPCodec.cpp
     */
    private int decodeSubSimple(int mps, long z) {
        long zz = z;
        if (zz > this.code) {
            /* LPS branch */
            zz = 0x10000 - zz;
            this.a = this.a + zz;
            this.code = this.code + zz;

            /* LPS renormalization */
            lpsRenormalization();

            adjustFence();
            return mps ^ 1;
        }
        else {
            /* MPS renormalization */
            this.scount -= 1;
            this.a = asUnsignedShort(zz << 1);
            this.code = asUnsignedShort((this.code << 1) | ((this.buffer >> this.scount) & 1));

            adjustFence();
            return mps;
        }
    }

    /* LPS renormalization */
    private void lpsRenormalization() {
        final int shift = ffz(this.a);
        this.scount -= shift;
        this.a = asUnsignedShort(this.a << shift);
        this.code = asUnsignedShort((this.code << shift) | ((this.buffer >> this.scount) & ((1L << shift) - 1)));
    }

    private void adjustFence() {
        if (this.scount < 16) {
            preload();
        }

        this.fence = Math.min(this.code, 0x7fff);
    }
}
