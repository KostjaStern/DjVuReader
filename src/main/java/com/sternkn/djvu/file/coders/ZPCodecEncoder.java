package com.sternkn.djvu.file.coders;

import java.io.Closeable;
import java.io.IOException;

public interface ZPCodecEncoder extends Closeable {

    /**
     * Encodes bit {bit} without compression (pass-thru encoder).
     * Argument {bit} must be #0# or #1#.
     * No compression will be applied. Calling this function always increases
     * the length of the code bit sequence by one bit.
     */
    void encoder(int bit);

    /**
     * Encodes bit {bit} using context variable {ctx}.
     * Argument {bit} must be #0# or #1#.
     * This function should only be used with ZP-Coder objects created for encoding.
     * It may modify the contents of variable {ctx} in order to perform context adaptation.
     **/
    void encoder(int bit, BitContext ctx);

    @Override
    void close() throws IOException;
}
