package com.sternkn.djvu.file.coders;

public interface ZPCodecDecoder {

    int decoder();

    int IWdecoder();

    int decoder(BitContext ctx);
}
