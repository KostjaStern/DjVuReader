package com.sternkn.djvu.gui.view_model;

import com.sternkn.djvu.model.ChunkInfo;
import com.sternkn.djvu.model.DjVuModel;
import javax.swing.SwingWorker;

public interface ChunkDecodingWorkerFactory {

    SwingWorker<ChunkInfo, Void> create(MainViewModel viewModel, DjVuModel djvuModel, long chunkId);
}
