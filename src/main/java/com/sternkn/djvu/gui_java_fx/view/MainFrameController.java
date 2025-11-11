package com.sternkn.djvu.gui_java_fx.view;

import com.sternkn.djvu.gui.view_model.ChunkTreeNode;
import com.sternkn.djvu.gui.view_model.TextZoneNode;
import com.sternkn.djvu.gui_java_fx.view_model.MainViewModel;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TreeView;
import javafx.scene.control.TextArea;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Locale;

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
    private TextField zoomValue;

    @FXML
    private ScrollPane imageScrollPane;

    @FXML
    private ImageView imageView;

    @FXML
    private TextField xCoordinate;

    @FXML
    private TextField yCoordinate;

    @FXML
    private BorderPane root;

    public MainFrameController(MainViewModel viewModel, Stage stage) {
        this.viewModel = viewModel;
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        LOG.debug("Initializing MainFrameController ... ");

        chunkTree.rootProperty().bind(viewModel.getChunkRootNode());
        chunkTree.setCellFactory(tv -> new ChunkTreeCell(viewModel, stage));

        topTextArea.textProperty().bind(viewModel.getTopText());

        progressBar.progressProperty().bind(viewModel.getProgress());

        textTree.rootProperty().bind(viewModel.getTextRootNode());
        textTree.visibleProperty().bind(viewModel.getShowTextTree());
        textTree.managedProperty().bind(viewModel.getShowTextTree());

        imageView.visibleProperty().bind(viewModel.getShowTextTree().not());
        imageView.managedProperty().bind(imageView.visibleProperty());

        bindZoom();
    }

    private void bindZoom() {
        TextFormatter<Double> zoomFormatter = new TextFormatter<>(
            change -> {
                String newValue = change.getControlNewText();
                Double value = null;
                try {
                    value = Double.parseDouble(newValue);
                }
                catch (NumberFormatException exception) {
                    return null;
                }

                if (value > 0 && value < 1000) {
                    return change;
                }
                return null;
            }
        );
        zoomValue.setTextFormatter(zoomFormatter);

        Bindings.bindBidirectional(zoomValue.textProperty(), viewModel.getZoom());

        zoomValue.focusedProperty().addListener((o, was, now) -> {
            if (!now && zoomFormatter.getValue() != null) {
                zoomValue.setText(String.format(Locale.ROOT, "%.2f", zoomFormatter.getValue()));
            }
        });

        viewModel.getZoom().addListener((observable, oldValue, newValue) -> {
            Image image = imageView.getImage();
            if (image == null || newValue == null) {
                return;
            }
            double scale = Double.parseDouble(newValue);
            double width = imageView.getFitWidth() > 0 ? imageView.getFitWidth() : image.getWidth();
            double height = imageView.getFitHeight() > 0 ? imageView.getFitHeight() : image.getHeight();
            LOG.debug("Zoom update: scale = {}", scale);
            LOG.debug("Zoom update: width = {}", width);
            LOG.debug("Zoom update: height = {}", height);

            double newWidth = width * scale;
            double newHeight = height * scale;
            LOG.debug("Zoom update: newWidth = {}", newWidth);
            LOG.debug("Zoom update: newHeight = {}", newHeight);

            imageView.setFitWidth(newWidth);
            imageView.setFitHeight(newHeight);
        });

        imageView.imageProperty().bind(viewModel.getImage());
    }

    @FXML
    private void onExit() {
        stage.close();
    }

    protected FileChooser createFileChooser() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select a DJVU file to open");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("DjVu files", "*.djvu")
        );

        return chooser;
    }

    @FXML
    private void onOpenFile() {
        FileChooser chooser = createFileChooser();

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
