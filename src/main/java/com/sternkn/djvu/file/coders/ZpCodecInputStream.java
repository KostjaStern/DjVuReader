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
    private static final int BYTE_SIZE = 8;

    private final InputStream inputStream;

    private final byte[] ffzt;
    private final ZpCodecTable[] table;

    // buffer related fields
    private byte delay;
    private int bufferSize;
    private int currentByte;
    private long buffer;

    private long fence;
    private long a;
    private long c;

    enum SymbolType {
        MPS, // The more probable symbol
        LPS  // The less probable symbol
    }

    /**
     * At the beginning of a chunk, the values of {a} and {c} are reinitialized. When the decoder is
     * decoding a chunk, it may require more bits than are present within the chunk's data. In
     * this case, all additional required bits are to be assumed by the decoder to be 1. If there are
     * excess bits at the end of a chunk, they are ignored.
     */
    public ZpCodecInputStream(InputStream inputStream) {
        this.inputStream = inputStream;

        this.ffzt = ZpCodecUtils.getFFZTable();
        this.table = ZpCodecUtils.getDefaultTable();

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
     */
    private void init() {
        this.a = 0;

        readNextByte();
        this.c = ((long) this.currentByte << 8);

        readNextByte();
        this.c = this.c | this.currentByte;

        /* Compute initial fence */
        this.fence = Math.min(this.c, 0x7fff);

        /* Preload buffer */
        this.delay = 25;
        this.bufferSize = 0;
        this.buffer = 0;
        preloadBuffer();
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

    private void preloadBuffer() {
        if (this.bufferSize >= 2 * BYTE_SIZE) return;

        while (this.bufferSize <= 3 * BYTE_SIZE) {
            if (readNextByte() == NO_MORE_BYTE) {
                --this.delay;

                LOG.debug("preload: delay = {}", this.delay);

                if (this.delay < 1) {
                    throw new DjVuFileException("End of djvu file");
                }
            }

            this.buffer = asUnsignedInt((this.buffer << BYTE_SIZE) | this.currentByte);
            this.bufferSize += BYTE_SIZE;
        }
    }

    /**
     *  In pass-through mode, the decoder is invoked with no input argument.
     *  No context is involved.
     *
     *  @return  the 1-bit value returned by the decoder.
     */
    @Override
    public int decoder() {
        int bit;

        final SymbolType symbolType;
        long z = 0x8000 + (this.a >> 1);

        if (z > this.c) {
            symbolType = SymbolType.LPS;

            z = 0x10000 - z;
            this.a = this.a + z;
            this.c = this.c + z;

            bit = 1;
        }
        else {
            symbolType = SymbolType.MPS;
            this.a = z;
            bit = 0;
        }

        renormalization(symbolType);
        return bit;
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

        long d = asUnsignedInt(0x6000 + ((zz + this.a) >> 2));
        if (zz > d) {
            zz = d;
        }

        final SymbolType symbolType;

        /* Test MPS/LPS */
        if (zz > this.c) {
            symbolType = SymbolType.LPS;
            zz = 0x10000 - zz;
            this.a = this.a + zz;
            this.c = this.c + zz;
            ctx.setValue(this.table[index].dn());

            bit = bit ^ 1;
        }
        else {
            symbolType = SymbolType.MPS;
            if (this.a >= this.table[index].m()) {
                ctx.setValue(this.table[index].up());
            }

            this.a = zz;
        }

        renormalization(symbolType);
        return bit;
    }

    /*
       When the values in the registers are too large, they must be renormalized.
    */
    private void renormalization(SymbolType symbolType) {
        final int shift = symbolType == SymbolType.MPS ? 1 : ffz(this.a);
        this.bufferSize -= shift;
        this.a = asUnsignedShort(this.a << shift);
        this.c = asUnsignedShort((this.c << shift) | ((this.buffer >> this.bufferSize) & ((1L << shift) - 1)));

        this.fence = Math.min(this.c, 0x7fff);

        preloadBuffer();
    }
}
