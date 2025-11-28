package com.sternkn.djvu.gui.view_model;

import com.sternkn.djvu.model.DjVuModel;
import com.sternkn.djvu.model.Page;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sternkn.djvu.utils.utils.ExceptionUtils.getStackTraceAsString;

public class PageLoadingTask extends Task<Page> {
    private static final Logger LOG = LoggerFactory.getLogger(PageLoadingTask.class);

    private final DjVuModel djvuModel;
    private final long offset;

    public PageLoadingTask(DjVuModel djvuModel, long offset) {
        this.djvuModel = djvuModel;
        this.offset = offset;
    }

    @Override
    protected Page call() {
        try {
            return djvuModel.getPage(offset);
        }
        catch (Exception e) {
            LOG.error(getStackTraceAsString(e));
            throw e;
        }
    }
}
