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
    It is my attempt to implement Z'-Codec only by description from DjVu Reference v3 specification.
 */
public class MyZpCodecInputStream implements ZPCodecDecoder, Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(MyZpCodecInputStream.class);

    private static final int NO_MORE_BYTE = -1;

    private final InputStream inputStream;

    private final ZpCodecTable[] table;

    private int currentByte;
    private long buffer;
    private byte delay;
    private int scount;

    private long a;
    private long c;

    /**
     * At the beginning of a chunk, the values of {a} and {code} are reinitialized. When the decoder is
     * decoding a chunk, it may require more bits than are present within the chunk's data. In
     * this case, all additional required bits are to be assumed by the decoder to be 1. If there are
     * excess bits at the end of a chunk, they are ignored.
     */
    public MyZpCodecInputStream(InputStream inputStream) {
        this.inputStream = inputStream;

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

        /* Preload buffer */
        this.delay = 25;
        this.scount = 0;
        this.buffer = 0;
        preload();
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
                    throw new DjVuFileException("Unexpected end of buffer");
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
        int b;
        // (this.a >> 1)
        // long z = 0x8000 + (this.a >> 1);
        long z = 0x8000 + ((this.a + this.a + this.a) >> 3);

        if (c > z) {
            b = 0;
            a = z;
        }
        else {
            b = 1;
            a = a + 0x10000 - z;
            c = c + 0x10000 - z;
        }

        renormalization();

        return b;
    }

    /*
     * When the values in the registers are too large, they must be renormalized.
     */
    private void renormalization() {
        while (a >= 0x8000) {
            a = asUnsignedShort((a << 1) - 0x10000);

            scount -= 1;
            c = asUnsignedShort(((c << 1) - - 0x10000) | ((this.buffer >> this.scount) & 1));
            preload();
        }
    }

    /**
     *
     */
    @Override
    public int decoder(BitContext ctx) {
        final int index = ctx.getValue();
        int b;

        if (index < 0 || index >= this.table.length ) {
            throw new IllegalArgumentException("The index should be in range 0 .. " + (this.table.length - 1));
        }

        long z = this.a + this.table[index].p();
        long d = asUnsignedInt(0x6000 + ((z + this.a) >> 2));
        if (z > d) {
            z = d;
        }

        /* Test MPS/LPS */
        if (this.c > z) {
            b = index & 1;  // B := K(i) (mod 2)

            if (this.a >= this.table[index].m()) {  // if (A >= θk(i)) { K(i) = μk(i) }
                ctx.setValue(this.table[index].up());
            }

            this.a = z;
        }
        else {
            b = 1 - (index & 1); // B := 1 - (K(i)(mod 2))

            this.a = this.a + 0x10000 - z; // A := A + 0x100000 - Z
            this.c = this.c + 0x10000 - z; // C := C + 0x100000 - Z

            ctx.setValue(this.table[index].dn()); // K(i) = λk(i)
        }

        renormalization();

        return b;
    }
}
