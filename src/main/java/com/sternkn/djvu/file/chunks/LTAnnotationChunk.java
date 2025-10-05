package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.coders.BSByteInputStream;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.sternkn.djvu.file.utils.InputStreamUtils.readString;
import static com.sternkn.djvu.file.utils.StringUtils.NL;

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
