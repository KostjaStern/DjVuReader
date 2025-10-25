package com.sternkn.djvu.model;

import java.io.File;

public interface DjVuModel {

    void saveChunkData(File file, long chunkId);

    ChunkInfo getChunkInfo(long chunkId);

    String getChunkStatistics();
}
