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
import com.sternkn.djvu.gui.view_model.PageNode;
import com.sternkn.djvu.gui.view_model.TextZoneNode;
import com.sternkn.djvu.gui.view_model.MainViewModel;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static com.sternkn.djvu.utils.ExceptionUtils.getStackTraceAsString;


public class MainFrameController {

    private static final Logger LOG = LoggerFactory.getLogger(MainFrameController.class);

    private final MainViewModel viewModel;
    private final Stage stage;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label progressMessage;

    @FXML
    private TreeView<ChunkTreeNode> chunkTree;

    @FXML
    private TreeView<TextZoneNode> textTree;

    @FXML
    private TextArea topTextArea;

    @FXML
    private ImageView imageView;

    @FXML
    private ImageView pageView;

    @FXML
    private Pane selectionOverlay;

    @FXML
    private VBox chunkInfoBox;

    @FXML
    private SplitPane chunksSplitPane;

    @FXML
    private SplitPane pagesSplitPane;

    @FXML
    private ListView<PageNode> pageList;

    @FXML
    private MenuItem navigationMenu;

    @FXML
    private MenuItem showStatisticsMenu;

    private final DoubleProperty sharedPos;

    private final Rectangle selection;
    private double startX;
    private double startY;

    public MainFrameController(MainViewModel viewModel, Stage stage) {
        this.viewModel = viewModel;
        this.stage = stage;
        sharedPos = new SimpleDoubleProperty(0.25);
        selection = createSelectionRectangle();
    }

    private Rectangle createSelectionRectangle() {
        Rectangle selection = new Rectangle();
        selection.setManaged(false);
        selection.setFill(Color.color(0, 0.5, 1, 0.20));
        selection.setStroke(Color.DODGERBLUE);
        selection.getStrokeDashArray().setAll(6.0, 6.0);
        selection.setVisible(false);

        return selection;
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

        pageView.imageProperty().bind(viewModel.getPageImage());
        pageView.fitWidthProperty().bind(viewModel.getFitWidth());

        pageList.getStyleClass().add("pages");
        pageList.itemsProperty().bind(viewModel.getPages());
        pageList.setCellFactory(v -> new PageCell());
        pageList.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, current) -> {
            if (current != null && !Objects.equals(current, old)) {
                LOG.debug("Page clicked: {}", current);
                viewModel.loadPageAsync(current);
            }
        });

        navigationMenu.disableProperty().bind(viewModel.disableNavigationMenu());
        showStatisticsMenu.disableProperty().bind(viewModel.disableStatisticsMenu());

        progressMessage.textProperty().bind(viewModel.getProgressMessage());

        bindDivider(chunksSplitPane);
        bindDivider(pagesSplitPane);

        Platform.runLater(() -> {
            viewModel.getFitWidth().set(chunkInfoBox.getWidth());
        });

        selectionOverlay.getChildren().add(selection);

        selectionOverlay.minWidthProperty().bind(pageView.boundsInParentProperty().map(Bounds::getWidth));
        selectionOverlay.minHeightProperty().bind(pageView.boundsInParentProperty().map(Bounds::getHeight));
        selectionOverlay.prefWidthProperty().bind(selectionOverlay.minWidthProperty());
        selectionOverlay.prefHeightProperty().bind(selectionOverlay.minHeightProperty());
        selectionOverlay.maxWidthProperty().bind(selectionOverlay.minWidthProperty());
        selectionOverlay.maxHeightProperty().bind(selectionOverlay.minHeightProperty());

        selectionOverlay.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onPressed);
        selectionOverlay.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onDragged);
        selectionOverlay.addEventHandler(MouseEvent.MOUSE_RELEASED, this::onReleased);
    }

    private void onPressed(MouseEvent e) {
        LOG.debug("onPressed: e.getX() = {}, e.getY() = {}, viewModel.getFitWidth() = {}",
                e.getX(), e.getY(), viewModel.getFitWidth().doubleValue());

        Image page = viewModel.getPageImage().getValue();
        if (page != null) {
            LOG.debug("Page width: {}, height = {}", page.getWidth(), page.getHeight());
        }

        startX = e.getX();
        startY = e.getY();

        LOG.debug("onPressed: startX = {}, startY = {}", startX, startY);

        selection.setX(startX);
        selection.setY(startY);
        selection.setWidth(0);
        selection.setHeight(0);
        selection.setVisible(true);
    }

    private void onDragged(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();

        double minX = Math.min(startX, x);
        double minY = Math.min(startY, y);
        double w = Math.abs(x - startX);
        double h = Math.abs(y - startY);

        selection.setX(minX);
        selection.setY(minY);
        selection.setWidth(w);
        selection.setHeight(h);
    }

    private void onReleased(MouseEvent e) {
        if (!selection.isVisible() ||
            selection.getWidth() < 2 ||
            selection.getHeight() < 2 ||
            pageView.getImage() == null) {
            selection.setVisible(false);
            return;
        }


        LOG.debug("onReleased: ROI in image pixels: x = {}, y = {}, w = {}, h = {}",
                e.getX(), e.getY(), Math.abs(e.getX() - startX), Math.abs(e.getY() - startY));

        // optional: оставить выделение или скрыть
        // selection.setVisible(false);

        // TODO: вызвать OCR и скопировать в буфер
        // String text = runOcrOnRegion(ix1, iy1, w, h);
        // copyToClipboard(text);
    }

    private void bindDivider(SplitPane splitPane) {
        var divider = splitPane.getDividers().getFirst();
        divider.positionProperty().bindBidirectional(sharedPos);
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
    private void onOpenNavigation() {
        LOG.debug("Opening navigation ...");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/TableOfContentsDialog.fxml"));

            final Stage dialogStage = new Stage();
            dialogStage.setTitle("Table of Contents");
            dialogStage.initOwner(stage);
            dialogStage.initModality(Modality.NONE);

            TableOfContentsDialogController controller = new TableOfContentsDialogController(
                    viewModel, pageList);
            loader.setController(controller);

            Scene scene = new Scene(loader.load());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        }
        catch (IOException e) {
            LOG.error(getStackTraceAsString(e));
            throw new RuntimeException(e);
        }
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
