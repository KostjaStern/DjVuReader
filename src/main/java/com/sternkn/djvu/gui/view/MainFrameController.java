package com.sternkn.djvu.gui.view;

import com.sternkn.djvu.gui.view_model.ChunkTreeNode;
import com.sternkn.djvu.gui.view_model.TextZoneNode;
import com.sternkn.djvu.gui.view_model.MainViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeView;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
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
    private TreeView<TextZoneNode> textTree;

    @FXML
    private TextArea topTextArea;

    @FXML
    private ImageView imageView;

    @FXML
    private VBox chunkInfoBox;

    public MainFrameController(MainViewModel viewModel, Stage stage) {
        this.viewModel = viewModel;
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        LOG.debug("Initializing MainFrameController ... ");

        chunkTree.rootProperty().bind(viewModel.getChunkRootNode());
        chunkTree.setCellFactory(tv -> new ChunkTreeCell(this));

        topTextArea.textProperty().bind(viewModel.getTopText());

        progressBar.progressProperty().bind(viewModel.getProgress());

        textTree.rootProperty().bind(viewModel.getTextRootNode());
        textTree.visibleProperty().bind(viewModel.getShowTextTree());
        textTree.managedProperty().bind(viewModel.getShowTextTree());

        imageView.visibleProperty().bind(viewModel.getShowTextTree().not());
        imageView.fitWidthProperty().bind(viewModel.getFitWidth());
        imageView.imageProperty().bind(viewModel.getImage());
        imageView.managedProperty().bind(imageView.visibleProperty());

        Platform.runLater(() -> {
            viewModel.getFitWidth().set(chunkInfoBox.getWidth());
        });
    }

    @FXML
    private void onExit() {
        stage.close();
    }

    MainViewModel getViewModel() {
        return viewModel;
    }

    Stage getStage() {
        return stage;
    }

    FileChooser openFileDialog() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select a DJVU file to open");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("DjVu files", "*.djvu")
        );

        return chooser;
    }

    FileChooser saveChunkDataDialog() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save chunk data as");

        return chooser;
    }

    @FXML
    private void onOpenFile() {
        FileChooser chooser = openFileDialog();

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
        viewModel.zoomIn();
    }

    @FXML
    private void onZoomOutClicked() {
        viewModel.zoomOut();
    }
}
