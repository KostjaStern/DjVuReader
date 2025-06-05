package com.sternkn.djvu.file;

import com.sternkn.djvu.file.chunks.Chunk;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

// import java.io.File;
import java.util.List;


public class DjVuFile {
    // private static final Logger LOG = LoggerFactory.getLogger(DjVuFile.class);

    private final MagicHeader header;
    private final List<Chunk> chunks;
    private final long fileSize;

    public DjVuFile(MagicHeader header, List<Chunk> chunks, long fileSize) {
        this.header = header;
        this.chunks = chunks;
        this.fileSize = fileSize;
    }

    public MagicHeader getHeader() {
        return header;
    }

    public List<Chunk> getChunks() {
        return chunks;
    }

    public long getFileSize() {
        return fileSize;
    }
}
