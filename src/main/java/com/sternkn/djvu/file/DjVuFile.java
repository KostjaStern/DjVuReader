package com.sternkn.djvu.file;

import com.sternkn.djvu.file.chunks.Chunk;
import com.sternkn.djvu.file.chunks.ChunkId;
import com.sternkn.djvu.file.chunks.DirectoryChunk;

import java.util.List;
import java.util.Map;

public interface DjVuFile {

    MagicHeader getHeader();

    List<Chunk> getChunks();

    long getFileSize();

    DirectoryChunk getDirectoryChunk();

    Chunk getChunkById(long chunkId);

    Chunk getChunkByOffset(long offset);

    Map<ChunkId, List<Chunk>> getAllPageChunks(Chunk chunk);

    List<Chunk> getAllImageChunks(Chunk chunk);

    Chunk findSharedShapeChunk(Chunk chunk);
}
