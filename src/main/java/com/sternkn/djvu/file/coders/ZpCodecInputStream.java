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

    // CPP types size
    assert(sizeof(unsigned int)==4);
    assert(sizeof(unsigned short)==2);
 */
public class ZpCodecInputStream implements ZPCodecDecoder, Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(ZpCodecInputStream.class);
    private static final int NO_MORE_BYTE = 0xFF;

    private final InputStream inputStream;

    private byte[] ffzt;

    //
    private int[] p;
    private int[] m;
    private int[] up;
    private int[] dn;

    private byte delay;
    private int scount;

    private int currentByte;

    private long fence;
    private long buffer;
    private long a;
    private long code;


    public ZpCodecInputStream(InputStream inputStream) {
        this.inputStream = inputStream;

        // Create machine independent ffz table
        this.ffzt = new byte[256];
        for (int i = 0; i < this.ffzt.length; i++) {
            this.ffzt[i] = 0;
            for (int j = i; (j & 0x80) != 0; j = j << 1) {
                this.ffzt[i] += 1;
            }
        }

        newTable();

        this.fence = 0;
        this.buffer = 0;

        init();
    }

    @Override
    public void close() throws IOException {
        this.inputStream.close();
    }

    private void newTable() {
        final ZpCodecTable[] defaultTable = ZpCodecUtils.getDefaultTable();
        final int size = defaultTable.length;

        this.p = new int[size];
        this.m = new int[size];
        this.up = new int[size];
        this.dn = new int[size];

        for (int index = 0; index < size; index++) {
            ZpCodecTable table = defaultTable[index];
            this.p[index] = table.p();
            this.m[index] = table.m();
            this.up[index] = table.up();
            this.dn[index] = table.dn();
        }
    }

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
        this.fence = this.code;
        if (this.code >= 0x8000) {
            this.fence = 0x7fff;
        }
    }

    private void readNextByte() {
        try {
            this.currentByte = this.inputStream.read();
        } catch (IOException e) {
            throw new DjVuFileException("We can not read next byte", e);
        }

        if (this.currentByte == -1) {
            this.currentByte = NO_MORE_BYTE;
        }
    }

    private void preload() {
        while (this.scount <= 24) {
            readNextByte();
            if (currentByte == NO_MORE_BYTE) {
                --this.delay;
                if (this.delay < 1) {
                    throw new DjVuFileException("End of djvu file");
                }
            }

            this.buffer = asUnsignedInt((this.buffer << 8) | this.currentByte);
            this.scount += 8;
        }
    }

    /**
     *
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
        int index = ctx.getValue();

        assert index < 0 || this.p.length >= index : "The index should be in range 0 .. " + (this.p.length - 1);

        long z = this.a + this.p[index];
        if (z <= this.fence) {
            this.a = z;
            return (index & 1);
        }

        return decodeSub(ctx, z);
    }

    private int ffz(long x) {
        int index1 = (int) (x & 0xff);
        int index2 = (int) ((x >> 8) & 0xff);
        return (x >= 0xff00) ? (this.ffzt[index1] + 8) : (this.ffzt[index2]);
    }

    /**
     *  see decode_sub from ZPCodec.cpp
     */
    private int decodeSub(BitContext ctx, long z) {
        long zz = z;
        int ind = ctx.getValue();
        int bit = (ind & 1);

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
            ctx.setValue(this.dn[ind]);

            /* LPS renormalization */
            lpsRenormalization();

            adjustFence();

            return bit ^ 1;
        }
        else
        {
            /* MPS adaptation */
            if (this.a >= this.m[ind]) {
                ctx.setValue(this.up[ind]);
            }

            /* MPS renormalization */
            this.scount -= 1;
            this.a = asUnsignedShort(zz << 1);
            this.code = asUnsignedShort((this.code << 1) | ((this.buffer >> this.scount) & 1));

            // bitcount += 1;

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

        this.fence = this.code;
        if (this.code >= 0x8000) {
            this.fence = 0x7fff;
        }
    }
}
