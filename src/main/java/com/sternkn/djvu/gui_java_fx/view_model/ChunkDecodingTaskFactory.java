package com.sternkn.djvu.gui_java_fx.view_model;

import com.sternkn.djvu.model.ChunkInfo;
import com.sternkn.djvu.model.DjVuModel;
import javafx.concurrent.Task;

public interface ChunkDecodingTaskFactory {
    Task<ChunkInfo> create(DjVuModel djvuModel, long chunkId);
}
