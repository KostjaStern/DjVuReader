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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.sternkn.djvu.file.coders.BSByteStreamUtils.CTXIDS;
import static com.sternkn.djvu.file.coders.BSByteStreamUtils.KILOBYTE;
import static com.sternkn.djvu.file.coders.BSByteStreamUtils.MAX_BLOCK_SIZE;
import static com.sternkn.djvu.file.coders.BSByteStreamUtils.getXMTF;
import static com.sternkn.djvu.utils.utils.NumberUtils.asUnsignedInt;
import static com.sternkn.djvu.utils.utils.NumberUtils.asUnsignedByte;

/*

 */
public class BSByteInputStream extends InputStream {

    private static final Logger LOG = LoggerFactory.getLogger(BSByteInputStream.class);

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

        this.bptr = 0;
        this.blocksize = 0;
        this.size = 0;
        this.ctx = new BitContext[300];

        for (int ind = 0; ind < this.ctx.length; ind++) {
            ctx[ind] = new BitContext();
        }
    }

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

        if (blocksize < size) {
            blocksize = size;
            if (gdata == null) {
                gdata = new int[blocksize];
            }
        }

        // Decode Estimation Speed
        if (zpDecoder.decoder() != 0) {
            zpDecoder.decoder();
        }

        int[] mtf = getXMTF();

        // Decode
        int mtfno = 3;
        int markerpos = -1;

        for (int index = 0; index < size; index++) {
            final int ctxid = Math.min(CTXIDS - 1, mtfno);

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

    @Override
    public int read() {
        byte[] bytes = new byte[1];
        int number = read(bytes);
        return number > 0 ? asUnsignedByte(bytes[0]) : number;
    }

    /**
     * Reads some number of bytes from the input stream and stores them into the buffer array {@code buffer}.
     * The number of bytes actually read is returned as an integer.
     *
     * @param buffer the buffer into which the data is read.
     * @return the total number of bytes read into the buffer, or {@code -1} if there is no more data
     *         because the end of the stream has been reached.
     */
    @Override
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

        while (sz > 0 && !eof) {
            // Decode if needed
            if (size == 0) {
                bptr = 0;
                if (decode() == 0) {
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
        }

        // Return copied bytes
        return copied;
    }

    @Override
    public int read(byte[] b, int off, int len) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] readAllBytes() {
        byte[] buffer = new byte[KILOBYTE];
        List<byte[]> data = new ArrayList<>();
        int number = read(buffer);
        while (number == KILOBYTE) {
            data.add(buffer);

            buffer = new byte[KILOBYTE];
            number = read(buffer);
        }

        int total = data.size() * KILOBYTE + number;

        byte[] result = new byte[total];
        int line = 0;
        for (byte[] arr : data) {
            System.arraycopy(arr, 0, result, line * KILOBYTE, arr.length);
            line++;
        }
        System.arraycopy(buffer, 0, result, line * KILOBYTE, number);

        return result;
    }

    @Override
    public byte[] readNBytes(int len) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long skip(long n) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void skipNBytes(long n) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int available() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long transferTo(OutputStream out) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() throws IOException {
        this.zpDecoder.close();
    }
}
