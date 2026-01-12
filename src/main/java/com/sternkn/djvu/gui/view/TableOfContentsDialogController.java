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
package com.sternkn.djvu.gui.view;

import com.sternkn.djvu.gui.view_model.MainViewModel;
import com.sternkn.djvu.model.MenuNode;
import com.sternkn.djvu.gui.view_model.PageNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class TableOfContentsDialogController {

    private static final Logger LOG = LoggerFactory.getLogger(TableOfContentsDialogController.class);

    private final MainViewModel viewModel;

    @FXML
    private TreeView<MenuNode> menuTree;

    private final ListView<PageNode> pageList;

    public TableOfContentsDialogController(MainViewModel viewModel, ListView<PageNode> pageList) {
        this.viewModel = viewModel;
        this.pageList = pageList;
    }

    @FXML
    private void initialize() {
        LOG.debug("Initializing TableOfContentsDialogController ...");

        menuTree.rootProperty().bind(viewModel.getMenuRootNode());
        menuTree.setCellFactory(tv -> new MenuTreeCell());
        menuTree.getSelectionModel().selectedItemProperty()
            .addListener((obs, old, current) -> {
            if (current != null && !Objects.equals(current, old)) {
                scrollToPage(current.getValue());
            }
        });
    }

    public void scrollToPage(MenuNode menuNode) {
        if (menuNode == null || menuNode.getPageNumber() == null) {
            return;
        }

        final int number = menuNode.getPageNumber() - 1;
        LOG.debug("scroll to page {}", number);

        Platform.runLater(() -> {
            pageList.getSelectionModel().select(number);
            pageList.scrollTo(number);
        });
    }
}
