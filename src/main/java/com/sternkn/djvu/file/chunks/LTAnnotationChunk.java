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
package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.coders.BSByteInputStream;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.sternkn.djvu.utils.utils.InputStreamUtils.readString;
import static com.sternkn.djvu.utils.utils.StringUtils.NL;

public class LTAnnotationChunk extends Chunk {

    private final String plainText;

    public LTAnnotationChunk(Chunk chunk) {
        super(chunk);
        this.plainText = encodeText();
    }

    private String encodeText() {
        InputStream byteStream = new ByteArrayInputStream(data);
        if (this.getChunkId() == ChunkId.LTAz) {
            byteStream = new BSByteInputStream(byteStream);
        }
        return readString(byteStream);
    }

    public String getPlainText() {
        return plainText;
    }

    @Override
    public String getDataAsText() {
        String parentData = super.getDataAsText();

        StringBuilder buffer = new StringBuilder();
        buffer.append(parentData).append(NL);

        buffer.append(" Plain text: ").append(plainText).append(NL).append(NL);

        return  buffer.toString();
    }
}
