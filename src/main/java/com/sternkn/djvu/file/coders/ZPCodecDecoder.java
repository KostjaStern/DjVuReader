package com.sternkn.djvu.file.coders;

import java.io.Closeable;
import java.io.IOException;

public interface ZPCodecDecoder extends Closeable {

    int decoder();

    int IWdecoder();

    /**
     * Decodes a bit using context variable {ctx}. This function should only be
     * used with ZP-Coder objects created for decoding. It may modify the
     * contents of variable {ctx} in order to perform context adaptation.
     **/
    int decoder(BitContext ctx);

    @Override
    void close() throws IOException;
}
