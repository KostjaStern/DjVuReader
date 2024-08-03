package com.sternkn.djvu.file.coders;

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

    private final DjVuFileReader fileReader;

    public ZpCodec(DjVuFileReader fileReader) {
        this.fileReader = fileReader;
    }

    
}
