package com.sternkn.djvu.gui_java_fx.view;

import com.sternkn.djvu.gui.view_model.ChunkTreeNode;
import com.sternkn.djvu.gui_java_fx.view_model.MainViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeView;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class MainFrameController {

    private static final Logger LOG = LoggerFactory.getLogger(MainFrameController.class);

    private final MainViewModel viewModel;
    private final Stage stage;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private TreeView<ChunkTreeNode> chunkTree;

    @FXML
    private TextArea topTextArea;

    @FXML
    private ImageView imageView;

    public MainFrameController(MainViewModel viewModel, Stage stage) {
        this.viewModel = viewModel;
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        LOG.debug("Initializing MainFrameController ... ");

        chunkTree.rootProperty().bind(viewModel.getChunkRootNode());
        topTextArea.textProperty().bind(viewModel.getTopText());
        progressBar.progressProperty().bind(viewModel.getProgress());
        imageView.imageProperty().bind(viewModel.getImage());
        imageView.scaleXProperty().bind(viewModel.getImageScale());
        imageView.scaleYProperty().bind(viewModel.getImageScale());

        chunkTree.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
            if (newValue != null) {
                viewModel.showChunkInfo(newValue.getValue().getChunkId());
            }
        });
    }

    @FXML
    private void onExit() {
        stage.close();
    }

    @FXML
    private void onOpenFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select a DJVU file to open");

        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("DjVu files", "*.djvu")
        );

        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            viewModel.loadFileAsync(file);
        }
    }

    @FXML
    private void onShowStatistics() {
        viewModel.showStatistics();
    }

    @FXML
    private void onAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About the program");
        alert.setHeaderText("DjVu Viewer");
        alert.setContentText("Version 0.2");
        alert.showAndWait();
    }

    @FXML
    private void onZoomInClicked() {
        // LOG.debug("Zoom In clicked ... ");
        viewModel.zoomIn();
    }

    @FXML
    private void onZoomOutClicked() {
        // LOG.debug("Zoom Out clicked ... ");
        viewModel.zoomOut();
    }
}
