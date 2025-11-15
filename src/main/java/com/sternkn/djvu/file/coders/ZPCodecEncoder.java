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
