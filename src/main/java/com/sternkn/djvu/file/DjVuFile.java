package com.sternkn.djvu.file;

import com.sternkn.djvu.file.chunks.Chunk;
import java.util.List;


public record DjVuFile(MagicHeader header, List<Chunk> chunks, long fileSize) {
}
