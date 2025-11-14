package com.sternkn.djvu.gui.view_model;

import com.sternkn.djvu.model.DjVuModel;
import com.sternkn.djvu.model.ChunkInfo;
import javafx.concurrent.Task;

public class ChunkDecodingTask extends Task<ChunkInfo> {

    private final DjVuModel djvuModel;
    private final long chunkId;

    public ChunkDecodingTask(DjVuModel djvuModel, long chunkId) {
        this.djvuModel = djvuModel;
        this.chunkId = chunkId;
    }

    @Override
    protected ChunkInfo call() throws Exception {
        return djvuModel.getChunkInfo(chunkId);
    }
}
