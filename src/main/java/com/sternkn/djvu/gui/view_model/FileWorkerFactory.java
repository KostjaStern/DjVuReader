package com.sternkn.djvu.gui.view_model;

import com.sternkn.djvu.file.DjVuFile;

import javax.swing.SwingWorker;
import java.io.File;

public interface FileWorkerFactory {

    SwingWorker<DjVuFile, Void> create(MainViewModel viewModel, File file);
}
