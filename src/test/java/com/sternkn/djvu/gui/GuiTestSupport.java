package com.sternkn.djvu.gui;

import com.sternkn.djvu.file.DjVuFile;
import com.sternkn.djvu.gui.view_model.MainViewModel;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;

public class GuiTestSupport {

    protected SwingWorker<DjVuFile, Void> createFileWorker(MainViewModel viewModel, File file, DefaultTreeModel model) {
        return new SwingWorker<>() {
            @Override
            protected DjVuFile doInBackground() {
                SwingUtilities.invokeLater(() -> {
                    viewModel.setTreeModel(model);
                    viewModel.setTitle(file.getName());
                    viewModel.setBusy(false);
                });
                return null;
            }
        };
    }
}
