package com.sternkn.djvu.gui.view_model;

import com.sternkn.djvu.model.DjVuModel;
import com.sternkn.djvu.model.Page;
import javafx.concurrent.Task;

public interface PageLoadingTaskFactory {
    Task<Page> create(DjVuModel djvuModel, long offset);
}
