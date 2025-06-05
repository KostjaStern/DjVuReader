package com.sternkn.djvu.file.coders;

import com.sternkn.djvu.file.ByteStream;
import com.sternkn.djvu.file.DjVuFileReader;

/*
    12 Appendix 3: Z´coding.
    The Z´-Coder is an approximate binary arithmetic coder.

    https://sourceforge.net/p/djvu/djvulibre-git/ci/master/tree/libdjvu/ZPCodec.h
    https://sourceforge.net/p/djvu/djvulibre-git/ci/master/tree/libdjvu/ZPCodec.cpp

    // CPP types size
    assert(sizeof(unsigned int)==4);
    assert(sizeof(unsigned short)==2);
 */
public class ZpCodec {

    // ZP CODER DEFAULT ADAPTATION TABLE
    // private static final ZpCodecTable[] defaultZTable = ZpCodecUtils.getDefaultTable();

    protected final ByteStream byteStream;

    // Direction (false=decoding, true=encoding)
    protected final boolean encoding;

    // machine independent ffz
    protected byte[] ffzt;

    //
    protected int[] p;
    protected int[] m;
    protected int[] up;
    protected int[] dn;

    protected int fence;
    protected int buffer;

    /*
        Variable bitcount counts the number of code bits processed by the coder since the construction of the object.
        This variable can be used to evaluate how many code bits are spent on various components of the message.
     */
    protected long bitcount;

    public ZpCodec(ByteStream byteStream, boolean encoding) {
        this.byteStream = byteStream;
        this.encoding = encoding;

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

        this.bitcount = 0;
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

//    void
//    BSByteStreamDecode::Decode::init(void)
//    {
//        gzp=ZPCodec::create(gbs,false,true);
//    }



//    inline int
//    ZPCodec::decoder(void)
//    {
//        return decode_sub_simple(0, 0x8000 + (a>>1));
//    }


}
