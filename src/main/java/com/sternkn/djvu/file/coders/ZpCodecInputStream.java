package com.sternkn.djvu.file.coders;

import com.sternkn.djvu.file.DjVuFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HexFormat;

import static com.sternkn.djvu.file.utils.NumberUtils.asUnsignedInt;
import static com.sternkn.djvu.file.utils.NumberUtils.asUnsignedShort;

/*
    12 Appendix 3: Z´coding.
    The Z´-Coder is an approximate binary arithmetic coder.

    Z-coder: a fast adaptive binary arithmetic coder
    https://patents.google.com/patent/US6476740B1/en

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

    /**
     * {fence} allows to provide improved decoding speed through a “fast path” design wherein decoding of
     * a most probable symbol (MPS) requires few computational steps.
     */
    private long fence;
    private long a;
    private long c;

    enum SymbolType {
        MPS, // more probable symbol
        LPS  // less probable symbol
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
        final int bit;
        final SymbolType symbolType;
        long z = 0x8000 + (this.a >> 1);
        dumpState(z, null);

        if (z > this.c) {
            symbolType = SymbolType.LPS;
            bit = 1;

            z = 0x10000 - z;
            this.a = this.a + z;
            this.c = this.c + z;
        }
        else {
            symbolType = SymbolType.MPS;
            bit = 0;

            this.a = z;
        }

        renormalization(symbolType);
        return bit;
    }

    private void dumpState(long z, BitContext ctx) {
        final String method = String.format("method: decoder(%s)", (ctx == null ? "" : ctx));

        HexFormat formatter = HexFormat.ofDelimiter(":").withUpperCase();
        final String fieldA = String.format("a : %s (%s)", this.a, formatter.toHexDigits(this.a));
        final String fieldC = String.format("c : %s (%s)", this.c, formatter.toHexDigits(this.c));
        final String fieldZ = String.format("z : %s (%s)", z, formatter.toHexDigits(z));
        final String fieldFence = String.format("fence : %s (%s)", this.fence, formatter.toHexDigits(this.fence));

        final String fieldBuffer = String.format("buffer : %s (%s)  , bufferSize = %s  , delay = %s",
                this.buffer, formatter.toHexDigits(this.buffer), this.bufferSize, this.delay);

        LOG.debug("{}\n{}   {}   {}\n{}\n--------- buffer info ---------\n{}\n\n",
                method, fieldA, fieldC, fieldZ, fieldFence, fieldBuffer);
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

        long z = this.a + table[index].p();
        dumpState(z, ctx);

        if (z <= this.fence) {
            this.a = z;
            return (index & 1);
        }

        final long d = asUnsignedInt(0x6000 + ((z + this.a) >> 2));
        if (z > d) {
            z = d;
        }

        final int bit;
        final SymbolType symbolType;

        /* Test MPS/LPS */
        if (z > this.c) {
            symbolType = SymbolType.LPS;
            bit = 1 - (index & 1); // B := 1 - (K(i)(mod 2))

            z = 0x10000 - z;
            this.a = this.a + z;
            this.c = this.c + z;
            ctx.setValue(table[index].dn());
        }
        else {
            symbolType = SymbolType.MPS;
            bit = (index & 1);  // B := K(i) (mod 2)

            if (this.a >= table[index].m()) {
                ctx.setValue(table[index].up());
            }
            this.a = z;
        }

        renormalization(symbolType);
        return bit;
    }

    private int ffz(long x) {
        final int lastByte = (int) (x & 0xff);
        final int penultimateByte = (int) ((x >> 8) & 0xff);
        return (x >= 0xff00) ? (this.ffzt[lastByte] + 8) : (this.ffzt[penultimateByte]);
    }

    /*
       When the values in the registers are too large, they must be renormalized.
    */
    private void renormalization(SymbolType symbolType) {
        final int shift = symbolType == SymbolType.MPS ? 1 : ffz(this.a);
        this.bufferSize -= shift;
        this.a = asUnsignedShort(this.a << shift);

        final long nextBit = (this.buffer >> this.bufferSize) & ((1L << shift) - 1);

        LOG.debug("renormalization: symbolType = {}, nextBit = {}, shift = {}", symbolType, nextBit, shift);

        this.c = asUnsignedShort((this.c << shift) | nextBit);

        this.fence = Math.min(this.c, 0x7fff);

        preloadBuffer();
    }
}
