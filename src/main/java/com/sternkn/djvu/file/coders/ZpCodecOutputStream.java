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

import java.io.IOException;
import java.io.OutputStream;

import static com.sternkn.djvu.file.utils.NumberUtils.asUnsignedShort;

public class ZpCodecOutputStream implements ZPCodecEncoder {

    private final ZpCodecTable[] table;

    private final OutputStream outputStream;

    // buffer related fields
    private int delay;
    private int scount;
    private int currentByte;
    private long buffer;
    private long nrun;

    private long a;
    private long subend;

    public ZpCodecOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;

        this.table = ZpCodecUtils.getDefaultTable();

        init();
    }

    @Override
    public void encoder(int bit) {
        if (bit != 0) {
            encode_lps_simple(0x8000 + (a >> 1));
        }
        else {
            encode_mps_simple(0x8000 + (a >> 1));
        }
    }

    private void encode_mps_simple(long z) {
        /* Code MPS */
        a = z;

        /* Export bits */
        if (a >= 0x8000) {
            zemit(1 - (subend >> 15) );
            subend = asUnsignedShort(subend << 1);
            a = asUnsignedShort(a << 1);
        }
    }

    private void encode_lps_simple(long z) {
        /* Code LPS */
        z = 0x10000 - z;
        subend += z;
        a += z;

        /* Export bits */
        while (a >= 0x8000) {
            zemit(1 - (subend >> 15) );
            subend = asUnsignedShort(subend << 1);
            a = asUnsignedShort(a << 1);
        }
    }

    private void zemit(long b) {
        /* Shift new bit into 3bytes buffer */
        buffer = (buffer << 1) + b;

        /* Examine bit going out of the 3bytes buffer */
        b = (buffer >> 24);
        buffer = (buffer & 0xffffff);

        /* The following lines have been changed in order to emphazise the
         * similarity between this bit counting and the scheme of Witten, Neal & Cleary
         * (WN&C).  Corresponding changes have been made in outbit and eflush.
         * Variable 'nrun' is similar to the 'bits_to_follow' in the W&N code.
         */
        switch((int) b)
        {
            /* Similar to WN&C upper renormalization */
            case 1:
                outbit(1);
                while (nrun-- > 0)
                    outbit(0);
                nrun = 0;
                break;

            /* Similar to WN&C lower renormalization */
            case 0xFF:
                outbit(0);
                while (nrun-- > 0)
                    outbit(1);
                nrun = 0;
                break;

            /* Similar to WN&C central renormalization */
            case 0:
                nrun += 1;
                break;
            default:
                throw new DjVuFileException("Invalid ZP codec encoder state (b = " + b + ")");
        }
    }

    private void outbit(int bit) {
        if (delay > 0) {
            if (delay < 0xff)
                delay -= 1;
        }
        else {
            /* Insert a bit */
            currentByte = (currentByte << 1) | bit;

            /* Output a byte */
            if (++scount == 8) {
                try {
                    this.outputStream.write(currentByte);
                }
                catch (IOException e) {
                    throw new DjVuFileException("ZPCodec.write_error", e);
                }

                scount = 0;
                currentByte = 0;
            }
        }
    }

    @Override
    public void encoder(int bit, BitContext ctx) {
        int index = ctx.getValue();
        long z = a + this.table[index].p();
        if (bit != (index & 1)) {
            encode_lps(ctx, z);
        }
        else if (z >= 0x8000) {
            encode_mps(ctx, z);
        }
        else {
            a = z;
        }
    }

    private void encode_mps(BitContext ctx, long z) {
        long d = 0x6000 + ((z + a) >> 2);
        if (z > d) {
            z = d;
        }

        int index = ctx.getValue();

        /* Adaptation */
        if (a >= this.table[index].m()) {
            ctx.setValue(this.table[index].up());
        }

        encode_mps_simple(z);
    }

    private void encode_lps(BitContext ctx, long z) {
        long d = 0x6000 + ((z + a) >> 2);
        if (z > d) {
            z = d;
        }

        /* Adaptation */
        ctx.setValue(table[ctx.getValue()].dn());

        /* Code LPS */
        encode_lps_simple(z);
    }

    private void init() {
        a = 0;
        scount = 0;
        currentByte = 0;
        delay = 25;
        subend = 0;
        buffer = 0xFFFFFF;
        nrun = 0;
    }

    private void flush() {
        /* adjust subend */
        if (subend > 0x8000) {
            subend = 0x10000;
        }
        else if (subend > 0) {
            subend = 0x8000;
        }

        /* zemit many mps bits */
        while (buffer != 0xffffff  || subend != 0) {
            zemit(1 - (subend >> 15) );
            subend = asUnsignedShort(subend << 1);
        }

        /* zemit pending run */
        outbit(1);
        while (nrun-- > 0) {
            outbit(0);
        }
        nrun = 0;

        /* zemit 1 until full byte */
        while (scount > 0) {
            outbit(1);
        }

        /* prevent further emission */
        delay = 0xff;
    }

    @Override
    public void close() throws IOException {
        flush();
        outputStream.close();
    }
}
