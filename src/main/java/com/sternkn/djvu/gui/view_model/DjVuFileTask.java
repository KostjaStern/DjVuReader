package com.sternkn.djvu.gui.view_model;

import com.sternkn.djvu.file.DjVuFile;
import com.sternkn.djvu.file.DjVuFileReader;
import javafx.concurrent.Task;
import java.io.File;

public class DjVuFileTask extends Task<DjVuFile> {

    private final File file;

    public DjVuFileTask(File file) {
        this.file = file;
    }

    @Override
    protected DjVuFile call() throws Exception {
        try (DjVuFileReader reader = new DjVuFileReader(file)) {
            return reader.readFile();
        }
    }
}
