package com.sternkn.djvu.file.coders;

import com.sternkn.djvu.file.ByteStream;
import com.sternkn.djvu.file.Data;
import com.sternkn.djvu.file.DjVuFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZpCodecDecode extends ZpCodec {
    private static final Logger LOG = LoggerFactory.getLogger(ZpCodecDecode.class);

    private int a;
    private int currentByte;
    private int code;

    private byte delay;
    private byte scount;

    public ZpCodecDecode(ByteStream byteStream) {
        super(byteStream, false);

        LOG.debug("ZpCodecDecode constructor start ...");

        this.a = 0;

        /* Read first 16 bits of code */
        this.currentByte = byteStream.readBytes(1).getFirstByte();
        this.code = (this.currentByte << 8);

        this.currentByte = byteStream.readBytes(1).getFirstByte();
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
        LOG.debug("ZpCodecDecode constructor end ...");
    }

    private void preload() {
        while (this.scount <= 24) {
            Data data = byteStream.readBytes(1);
            if (data.size() < 1) {
                --this.delay;
                if (this.delay < 1) {
                    throw new DjVuFileException("End of djvu file");
                }
            }
            this.currentByte = data.getFirstByte();
            this.buffer = (this.buffer << 8) | this.currentByte;
            this.scount += 8;
        }
    }

//    inline int ZPCodec::decoder(void) {
//        return decode_sub_simple(0, 0x8000 + (a>>1));
//    }
    public int decoder() {
        return decodeSubSimple(0, 0x8000 + (this.a >> 1));
    }

    public int decoder(int index) {
        assert index < 0 || this.p.length >= index : "The index should be in range 0 .. " + (this.p.length - 1);

        int z = this.a + this.p[index];
        if (z <= this.fence) {
            this.a = z;
            return (index & 1);
        }

        // int  decode_sub(BitContext &ctx, unsigned int z);
        // return decode_sub(ctx, z);
        return decodeSub(index, z);
    }

    private int ffz(int x) {
        return (x >= 0xff00) ? (this.ffzt[x & 0xff] + 8) : (this.ffzt[(x >> 8) & 0xff]);
    }

    /**
     *  see decode_sub from ZPCodec.cpp
     */
    private int decodeSub(int index, int z) {
        int zz = z;
        int ind = index;
        int bit = (ind & 1);

        /* Avoid interval reversion (#ifdef ZPCODER) */
        int d = 0x6000 + ((zz + this.a) >> 2);
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
            ind = this.dn[ind];

            /* LPS renormalization */
            lpsRenormalization();

            adjustFence();

            return bit ^ 1;
        }
        else
        {
            /* MPS adaptation */
            if (this.a >= this.m[ind]) {
                ind = this.up[ind];
            }

            /* MPS renormalization */
            this.scount -= 1;
            this.a = (zz << 1);
            this.code = (this.code << 1) | ((this.buffer >> this.scount) & 1);

            bitcount += 1;

            adjustFence();

            return bit;
        }
    }

    /**
     *  see decode_sub_simple from ZPCodec.cpp
     */
    private int decodeSubSimple(int mps, int z) {
        int zz = z;
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
            this.a = zz << 1;
            this.code = (this.code << 1) | ((this.buffer >> this.scount) & 1);

            this.bitcount += 1;

            adjustFence();
            return mps;
        }
    }

    /* LPS renormalization */
    private void lpsRenormalization() {
        final int shift = ffz(this.a);
        this.scount -= shift;
        this.a = (this.a << shift);
        this.code = (this.code << shift) | ((this.buffer >> this.scount) & ((1 << shift) - 1));

        bitcount += shift;
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
