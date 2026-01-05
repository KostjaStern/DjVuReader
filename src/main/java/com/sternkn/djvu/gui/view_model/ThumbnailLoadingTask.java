/*
    Copyright (C) 2025 Kostya Stern

    This program is free software; you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation; either version 2 of the License, or (at your option)
    any later version.

    This program is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
    more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc., 51
    Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
*/
package com.sternkn.djvu.gui.view_model;

import com.sternkn.djvu.model.DjVuModel;
import com.sternkn.djvu.model.Page;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sternkn.djvu.utils.ExceptionUtils.getStackTraceAsString;
import static com.sternkn.djvu.utils.ImageUtils.resize;

public class ThumbnailLoadingTask extends Task<Void> {
    private static final Logger LOG = LoggerFactory.getLogger(ThumbnailLoadingTask.class);

    private final MainViewModel model;
    private final DjVuModel djvuModel;

    public ThumbnailLoadingTask(MainViewModel model, DjVuModel djvuModel) {
        this.model = model;
        this.djvuModel = djvuModel;
    }

    @Override
    public Void call() {
        try {
            ListProperty<PageNode> pages = model.getPages();
            if (pages == null) {
                return null;
            }

            ObservableList<PageNode> pgs = pages.getValue();
            int pageCount = pgs.size();

            Platform.runLater(() -> {
                model.setProgressMessage("Loading page thumbnail ...");
                model.setProgress(0);
            });

            for (int index = 0; index < pageCount; index++) {
                if (this.isCancelled()) {
                    break;
                }

                final int pageNumber = index + 1;
                final PageNode pageNode = pages.get(index);
                final double progress = (double) pageNumber / pageCount;
                Page page = djvuModel.getPage(pageNode.getPage());
                Image thumbnail = resize(page.getImage(), PageNode.WIDTH, PageNode.HEIGHT);
                page.setImage(null);

                Platform.runLater(() -> {
                    pageNode.setThumbnail(thumbnail);
                    model.setProgress(progress);
                    model.setProgressMessage("Loading page thumbnail (page " + pageNumber + ") ...");
                });
            }

            Platform.runLater(() -> {
                model.setProgressMessage("");
                model.setProgress(0);
            });
            return null;
        }
        catch (Exception e) {
            LOG.error(getStackTraceAsString(e));
            throw e;
        }
    }
}
