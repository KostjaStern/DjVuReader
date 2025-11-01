package com.sternkn.djvu.gui_java_fx.view_model;

import com.sternkn.djvu.file.DjVuFile;
import javafx.concurrent.Task;
import java.io.File;

public interface FileTaskFactory {
    Task<DjVuFile> create(File file);
}
