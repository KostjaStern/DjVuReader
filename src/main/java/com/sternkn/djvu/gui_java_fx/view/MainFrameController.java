package com.sternkn.djvu.gui_java_fx.view;

import com.sternkn.djvu.gui.view_model.ChunkTreeNode;
import com.sternkn.djvu.gui.view_model.TextZoneNode;
import com.sternkn.djvu.gui_java_fx.view_model.MainViewModel;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Control;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TreeView;
import javafx.scene.control.TextArea;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
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

//    @FXML
//    private StackPane imageStackPane;

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


        // zoomValue.textProperty().bind(viewModel.getImageScale().asString());

        imageView.visibleProperty().bind(viewModel.getShowTextTree().not());
        imageView.managedProperty().bind(imageView.visibleProperty());
        imageView.imageProperty().bind(viewModel.getImage());
        imageView.xProperty().addListener((observable, oldValue, newValue) -> {
            LOG.debug("Image-X = {}", newValue);
        });
        imageView.yProperty().addListener((observable, oldValue, newValue) -> {
            LOG.debug("Image-Y = {}", newValue);
        });
        // xCoordinate.textProperty().bind(imageView.layoutXProperty().asString());
        // yCoordinate.textProperty().bind(imageView.layoutYProperty().asString());

        // imageView.scaleXProperty().bind(viewModel.getZoom(), new StringN);
        // imageView.scaleYProperty().bind(viewModel.getImageScale());

//        imageScrollPane.viewportBoundsProperty().addListener((obs, oldV, v) -> {
//            imageView.setFitWidth(v.getWidth());
//            imageView.setFitHeight(v.getHeight());
//        });

        bindZoom();
    }

    private void bindZoom() {
        StringConverter<Number> converter = new NumberStringConverter();
        Bindings.bindBidirectional(viewModel.getZoom(), imageView.scaleXProperty(), converter);
        Bindings.bindBidirectional(viewModel.getZoom(), imageView.scaleYProperty(), converter);

        // private TextFormatter<Double> zoomFormatter;
        TextFormatter<Double> zoomFormatter = new TextFormatter<>(
            change -> {
                String newValue = change.getControlNewText();
                Double value = null;
                try {
                    value = Double.parseDouble(newValue);
                }
                catch (NumberFormatException exception) {
                    LOG.debug("Zoom refused value (not number) = {}", change);
                    return null;
                }

                if (value > 0 && value < 1000) {
                    LOG.debug("Zoom valid value = {}", change);
                    return change;
                }

                LOG.debug("Zoom refused value = {}", change);
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
        // LOG.debug("Zoom In clicked ... ");
        viewModel.zoomIn();
    }

    @FXML
    private void onZoomOutClicked() {
        // LOG.debug("Zoom Out clicked ... ");
        viewModel.zoomOut();
    }

    private void dump(String name, Control control) {
        boolean isEditable = control instanceof TextInputControl tic && tic.isEditable();
        LOG.debug("{} : disabled = {}, editable = {}, focusTraversable = {}, mouseTransparent = {}",
            name, control.isDisabled(), isEditable, control.isFocusTraversable(), control.isMouseTransparent());
    }
}
