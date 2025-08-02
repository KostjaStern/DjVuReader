package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.coders.BSByteInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.sternkn.djvu.file.utils.InputStreamUtils.readString;

public class AnnotationChunk extends Chunk {
    private static final Logger LOG = LoggerFactory.getLogger(AnnotationChunk.class);

    private final String plainText;

    public AnnotationChunk(Chunk chunk) {
        super(chunk);
        this.plainText = encodeText();
    }

    private String encodeText() {
        InputStream byteStream = new ByteArrayInputStream(data);
        if (this.getChunkId() == ChunkId.ANTz) {
            byteStream = new BSByteInputStream(byteStream);
        }
        return readString(byteStream);
    }

    public String getPlainText() {
        return plainText;
    }
}
