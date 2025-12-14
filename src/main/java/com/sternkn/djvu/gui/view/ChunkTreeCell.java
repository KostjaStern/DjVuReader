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

import com.sternkn.djvu.gui.view_model.ChunkTreeNode;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Objects;

/*
    https://openjfx.io/javadoc/23/javafx.controls/javafx/scene/control/Cell.html
 */
public class ChunkTreeCell extends TreeCell<ChunkTreeNode> {
    private static final Logger LOG = LoggerFactory.getLogger(ChunkTreeCell.class);

    final static String SAVE_CHUNK_DATA = "Save chunk data as ...";

    private final ImageView imageView;
    private final Image imgClosed;
    private final Image imgOpen;
    private final Image imgDoc;

    private final ChangeListener<Boolean> expandedListener;
    private final MainFrameController frameController;


    public ChunkTreeCell(MainFrameController controller) {
        this.frameController = controller;

        imageView = new ImageView();
        imageView.setFitWidth(24);
        imageView.setFitHeight(24);
        imageView.setPreserveRatio(true);

        imgClosed = getImage("closed_folder_24x24.png");
        imgOpen   = getImage("open_folder_24x24.png");
        imgDoc    = getImage("document_24x24.png");

        expandedListener = (obs, oldV, isExpanded) -> {
            TreeItem<ChunkTreeNode> treeItem = getTreeItem();
            if (treeItem != null && !treeItem.isLeaf()) {
                imageView.setImage(isExpanded ? imgOpen : imgClosed);
            }
        };

        addContextMenu();

        this.selectedProperty().addListener((observable, oldValue, isSelected) -> {
            if (isSelected) {
                frameController.getViewModel().showChunkInfo(this.getItem().getChunkId());
            }
        });
    }

    private Image getImage(String path) {
        final URL url = getClass().getResource("/icons/" + path);
        return new Image(Objects.requireNonNull(url).toExternalForm());
    }

    @Override
    protected void updateItem(ChunkTreeNode item, boolean empty) {
        super.updateItem(item, empty);
        setDisclosureNode(null);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        setText(item.getNodeName());

        TreeItem<ChunkTreeNode> treeItem = getTreeItem();
        if (treeItem.isLeaf()) {
            imageView.setImage(imgDoc);
        }
        else {
            imageView.setImage(treeItem.isExpanded() ? imgOpen : imgClosed);
            treeItem.expandedProperty().addListener(expandedListener);
        }
        setGraphic(imageView);
    }

    private ContextMenu getChunkContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem saveChunk = new MenuItem(SAVE_CHUNK_DATA);
        contextMenu.getItems().addAll(saveChunk);

        saveChunk.setOnAction(e -> {
            ChunkTreeNode chunkNode = this.getItem();
            String fileName = String.format("%s_%s.data", chunkNode.getChunkName(), chunkNode.getChunkId());

            FileChooser fileChooser = frameController.saveChunkDataDialog();
            fileChooser.setInitialFileName(fileName);
            File file = fileChooser.showSaveDialog(frameController.getStage());
            if (file != null) {
                frameController.getViewModel().saveChunkData(file,  chunkNode.getChunkId());
            }
        });

        return contextMenu;
    }

    private void addContextMenu() {
        emptyProperty().addListener((obs, wasEmpty, isEmpty) -> {
            ChunkTreeNode chunkNode = this.getItem();

            if (!isEmpty && chunkNode != null && !chunkNode.isComposite()) {
                LOG.debug("Chunk context menu: chunkNode = {}", chunkNode);

                ContextMenu contextMenu = getChunkContextMenu();
                setContextMenu(contextMenu);
                return;
            }

            setContextMenu(null);
        });
    }
}
