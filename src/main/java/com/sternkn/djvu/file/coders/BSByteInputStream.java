package com.sternkn.djvu.file.coders;

import com.sternkn.djvu.file.DjVuFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Arrays;

import static com.sternkn.djvu.file.utils.NumberUtils.asUnsignedInt;
/*
    BSByteStream -> ByteStream -> GPEnabled
 */
public class BSByteInputStream {

    private static final Logger LOG = LoggerFactory.getLogger(BSByteInputStream.class);

    private static final int[] XMTF = {
        0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
        0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F,
        0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F,
        0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A, 0x3B, 0x3C, 0x3D, 0x3E, 0x3F,
        0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F,
        0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5A, 0x5B, 0x5C, 0x5D, 0x5E, 0x5F,
        0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6A, 0x6B, 0x6C, 0x6D, 0x6E, 0x6F,
        0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7A, 0x7B, 0x7C, 0x7D, 0x7E, 0x7F,
        0x80, 0x81, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8A, 0x8B, 0x8C, 0x8D, 0x8E, 0x8F,
        0x90, 0x91, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9A, 0x9B, 0x9C, 0x9D, 0x9E, 0x9F,
        0xA0, 0xA1, 0xA2, 0xA3, 0xA4, 0xA5, 0xA6, 0xA7, 0xA8, 0xA9, 0xAA, 0xAB, 0xAC, 0xAD, 0xAE, 0xAF,
        0xB0, 0xB1, 0xB2, 0xB3, 0xB4, 0xB5, 0xB6, 0xB7, 0xB8, 0xB9, 0xBA, 0xBB, 0xBC, 0xBD, 0xBE, 0xBF,
        0xC0, 0xC1, 0xC2, 0xC3, 0xC4, 0xC5, 0xC6, 0xC7, 0xC8, 0xC9, 0xCA, 0xCB, 0xCC, 0xCD, 0xCE, 0xCF,
        0xD0, 0xD1, 0xD2, 0xD3, 0xD4, 0xD5, 0xD6, 0xD7, 0xD8, 0xD9, 0xDA, 0xDB, 0xDC, 0xDD, 0xDE, 0xDF,
        0xE0, 0xE1, 0xE2, 0xE3, 0xE4, 0xE5, 0xE6, 0xE7, 0xE8, 0xE9, 0xEA, 0xEB, 0xEC, 0xED, 0xEE, 0xEF,
        0xF0, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9, 0xFA, 0xFB, 0xFC, 0xFD, 0xFE, 0xFF
    };

    // Limits on block sizes
    private static final long MAX_BLOCK_SIZE = 4096 * 1024; // 4M
    private static final long MIN_BLOCK_SIZE = 10 * 1024;   // 10K

    private static final int FREQMAX = 4;
    private static final int CTXIDS = 3;

    private long offset;
    private int bptr;
    private int blocksize;
    private int size;

    /** Context variable.
     Variables of type #BitContext# hold a single byte describing how to encode
     or decode message bits with similar statistical properties.  This single
     byte simultaneously represents the current estimate of the bit probability
     distribution (which is determined by the frequencies of #1#s and #0#s
     already coded with this context) and the confidence in this estimate
     (which determines how fast the estimate can change.)

     A coding program typically allocates hundreds of context variables.  Each
     coding context is initialized to zero before encoding or decoding.  Value
     zero represents equal probabilities for #1#s and #0#s with a minimal
     confidence and therefore a maximum adaptation speed.  Each message bit is
     encoded using a coding context determined as a function of previously
     encoded message bits.  The decoder therefore can examine the previously
     decoded message bits and decode the current bit using the same context as
     the encoder.  This is critical for proper decoding.
     */
    private BitContext[] ctx;
    private boolean eof;

    private int[] gdata;

    private final ZPCodecDecoder zpDecoder;


    public BSByteInputStream(InputStream inputStream) {
        this.zpDecoder = new ZpCodecInputStream(inputStream);

        this.offset = 0;
        this.bptr = 0;
        this.blocksize = 0;
        this.size = 0;
        this.ctx = new BitContext[300];

        for (int ind = 0; ind < this.ctx.length; ind++) {
            ctx[ind] = new BitContext();
        }
    }

//    public static void main(String ... args) {
//         byte b = (byte) 0xFF;
//        System.out.println("b = " + b); // b = -1
//
//        byte[] arr = new byte[4];
//        System.out.println("arr[2] = " + arr[2]); // arr[2] = 0
//    }

    /*
        see  int decode_raw(ZPCodec &zp, int bits) implementation from BSByteStream.cpp
     */
    private int decodeRaw(int bits, int ... bitContextIndex) {
        int n = 1;
        final int m = (1 << bits);
        while (n < m) {
            final int b;
            if (bitContextIndex.length > 0) {
                b = zpDecoder.decoder(ctx[bitContextIndex[0] + n]);
            }
            else {
                b = zpDecoder.decoder();
            }
            n = (n << 1) | b;
        }
        return n - m;
    }

    private void updateData(int[] mtf, int mtfno, int dataIndex) {
        gdata[dataIndex] = mtf[mtfno];

        for (int k = mtfno; k > 0; k--) {
            mtf[k] = mtf[k - 1];
        }
        mtf[0] = gdata[dataIndex];
    }

    private int decode() {

        this.size = decodeRaw(24);
        LOG.debug("Decoded block size = {}", this.size);

        if (this.size == 0) {
            return 0;
        }

        if (this.size > MAX_BLOCK_SIZE) {
            throw new DjVuFileException("Too big BZZ block size: " + this.size +
                    ". It should be between 10K and 4M");
        }

        if (blocksize < size)
        {
            blocksize = size;
            if (gdata == null) {
                gdata = new int[blocksize];
            }
        }

        // Decode Estimation Speed
        if (zpDecoder.decoder() != 0)
        {
            zpDecoder.decoder();
        }

        int[] mtf = Arrays.copyOf(XMTF, XMTF.length);


        // Decode
        int mtfno = 3;
        int markerpos = -1;

        for (int index = 0; index < size; index++) {
            int ctxid = CTXIDS - 1;
            if (ctxid > mtfno) {
                ctxid = mtfno;
            }

            if (zpDecoder.decoder(ctx[ctxid]) != 0) {
                mtfno = 0;
                updateData(mtf, mtfno, index);
                continue;
            }

            if (zpDecoder.decoder(ctx[CTXIDS + ctxid]) != 0) {
                mtfno = 1;
                updateData(mtf, mtfno, index);
                continue;
            }

            if (zpDecoder.decoder(ctx[2 * CTXIDS]) != 0) {
                mtfno = 2 + decodeRaw(1, 2 * CTXIDS);
                updateData(mtf, mtfno, index);
                continue;
            }

            if (zpDecoder.decoder(ctx[2 * CTXIDS + 2]) != 0) {
                mtfno = 4 + decodeRaw(2, 2 * CTXIDS + 2);
                updateData(mtf, mtfno, index);
                continue;
            }

            if (zpDecoder.decoder(ctx[2 * CTXIDS + 6]) != 0) {
                mtfno = 8 + decodeRaw(3, 2 * CTXIDS + 6);
                updateData(mtf, mtfno, index);
                continue;
            }

            if (zpDecoder.decoder(ctx[2 * CTXIDS + 14]) != 0) {
                mtfno = 16  + decodeRaw(4, 2 * CTXIDS + 14);
                updateData(mtf, mtfno, index);
                continue;
            }

            if (zpDecoder.decoder(ctx[2 * CTXIDS + 30]) != 0) {
                mtfno = 32 + decodeRaw(5, 2 * CTXIDS + 30);
                updateData(mtf, mtfno, index);
                continue;
            }

            if (zpDecoder.decoder(ctx[2 * CTXIDS + 62]) != 0) {
                mtfno = 64 + decodeRaw(6, 2 * CTXIDS + 62);
                updateData(mtf, mtfno, index);
                continue;
            }

            if (zpDecoder.decoder(ctx[2 * CTXIDS + 126]) != 0) {
                mtfno = 128 + decodeRaw(7, 2 * CTXIDS + 126);
                updateData(mtf, mtfno, index);
                continue;
            }

            mtfno = 256;
            gdata[index] = 0;
            markerpos = index;
        }

        /////////////////////////////////
        ////////// Reconstruct the string

        if (markerpos < 1 || markerpos >= size) {
            throw new DjVuFileException(String.format("It is corrupted bzz byte stream (size = %s, markerpos = %s)",
                    size, markerpos));
        }

        // Prepare count buffer
        long[] posn = new long[blocksize];
        int[] count = new int [256];

        // Fill count buffer
        for (int i = 0; i < size; i++) {
            if (i == markerpos) {
                continue;
            }
            int c = gdata[i];
            posn[i] = asUnsignedInt(((long) c << 24) | (count[c] & 0xffffff));
            count[c] += 1;
        }

        // Compute sorted char positions
        int last = 1;
        for (int i = 0; i < 256; i++) {
            int tmp = count[i];
            count[i] = last;
            last += tmp;
        }

        // Undo the sort transform
        int ind = 0;
        last = size - 1;
        while (last > 0) {
            long n = posn[ind];
            int c = (int) (posn[ind] >> 24);
            gdata[--last] = c;
            ind = (int) (count[c] + (n & 0xffffff));
        }

        // Free and check
        if (ind != markerpos) {
            throw new DjVuFileException(String.format("It is corrupted bzz byte stream (ind = %s, markerpos = %s)",
                    ind, markerpos));
        }

        return size;
    }

    /**
     *
     * @param buffer
     * @return
     */
    public int read(byte[] buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException("buffer can not be null");
        }

        if (eof) {
            return -1;
        }

        int copied = 0;
        int sz = buffer.length;
        int bufferPtr = 0;

        while (sz > 0 && !eof)
        {
            // Decode if needed
            if (size == 0)
            {
                bptr = 0;
                if (decode() == 0)
                {
                    size = 1 ;
                    eof = true;
                }
                size -= 1;
            }

            // Compute remaining
            int bytes = Math.min(size, sz);

            // Transfer
            for (int ind = 0; ind < bytes; ind++) {
                buffer[bufferPtr] = (byte)(gdata[ind + bptr] & 0xFF);
                bufferPtr++;
            }

            size -= bytes;
            bptr += bytes;
            sz -= bytes;
            copied += bytes;
            offset += bytes;
        }

        // Return copied bytes
        return copied;
    }
}
