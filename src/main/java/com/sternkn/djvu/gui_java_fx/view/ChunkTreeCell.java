package com.sternkn.djvu.gui_java_fx.view;

import com.sternkn.djvu.gui.view_model.ChunkTreeNode;
import com.sternkn.djvu.gui_java_fx.view_model.MainViewModel;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.Objects;

/*
    https://openjfx.io/javadoc/23/javafx.controls/javafx/scene/control/Cell.html
 */
public class ChunkTreeCell extends TreeCell<ChunkTreeNode> {

    private final ImageView imageView;
    private final Image imgClosed;
    private final Image imgOpen;
    private final Image imgDoc;

    private final ChangeListener<Boolean> expandedListener;

    private final MainViewModel viewModel;
    private final Stage stage;


    public ChunkTreeCell(MainViewModel viewModel, Stage stage) {
        this.viewModel = viewModel;
        this.stage = stage;

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
                viewModel.showChunkInfo(this.getItem().getChunkId());
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
            // treeItem.expandedProperty().removeListener(expandedListener); ???
            treeItem.expandedProperty().addListener(expandedListener);
        }
        setGraphic(imageView);
    }

    private void addContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem saveChunk = new MenuItem("Save chunk data as ...");
        contextMenu.getItems().addAll(saveChunk);

        saveChunk.setOnAction(e -> {
            ChunkTreeNode chunkNode = this.getItem();
            String fileName = String.format("%s_%s.data", chunkNode.getChunkName(), chunkNode.getChunkId());

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save chunk  data as");
            fileChooser.setInitialFileName(fileName);
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                viewModel.saveChunkData(file,  chunkNode.getChunkId());
            }
        });

        emptyProperty().addListener((obs, wasEmpty, isEmpty) -> {
            ChunkTreeNode chunkNode = this.getItem();
            if (!isEmpty && !chunkNode.isComposite()) {
                setContextMenu(contextMenu);
            }
            else {
                setContextMenu(null);
            }
        });
    }
}
