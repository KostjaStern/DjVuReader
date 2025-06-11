package com.sternkn.djvu.file.coders;

public interface ZPCodecDecoder {

    int decoder();

    int decoder(BitContext ctx);
}
