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

import com.sternkn.djvu.file.chunks.GRectangle;
import com.sternkn.djvu.gui.view_model.ChunkTreeNode;
import com.sternkn.djvu.gui.view_model.PageNode;
import com.sternkn.djvu.gui.view_model.TextZoneNode;
import com.sternkn.djvu.gui.view_model.MainViewModel;
import com.sternkn.djvu.model.PageData;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeView;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static com.sternkn.djvu.utils.ExceptionUtils.getStackTraceAsString;


public class MainFrameController implements PageScrolling {

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
    private VBox pageBox;

    @FXML
    private ScrollPane pageScrollPane;

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

    @FXML
    private ComboBox<Integer> pageSelector;

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

        pageView.imageProperty().bind(viewModel.getPageData().map(PageData::image));
        pageView.fitWidthProperty().bind(viewModel.getFitWidth());

        pageSelector.itemsProperty().bind(viewModel.getPagesIndex());
        pageSelector.getSelectionModel().selectedItemProperty().addListener((observable, old, current) -> {
            if (current != null && !Objects.equals(current, old)) {
                int currentIndex = current - 1;
                LOG.debug("Current page index: {}", currentIndex);
                goToPage(currentIndex);
            }
        });
        pageList.getStyleClass().add("pages");
        pageList.itemsProperty().bind(viewModel.getPages());
        pageList.setCellFactory(v -> new PageCell());
        pageList.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, current) -> {
            if (current != null && !Objects.equals(current, old)) {
                LOG.debug("Page clicked: {}", current);
                pageSelector.setValue(current.getIndex());
                viewModel.loadPageAsync(current);
            }
        });

        navigationMenu.disableProperty().bind(viewModel.disableNavigationMenu());
        showStatisticsMenu.disableProperty().bind(viewModel.disableStatisticsMenu());

        progressMessage.textProperty().bind(viewModel.getProgressMessage());

        bindDivider(chunksSplitPane);
        bindDivider(pagesSplitPane);

        Platform.runLater(() -> {
            viewModel.getFitWidth().set(pageBox.getWidth());
        });

        selectionOverlay.getChildren().add(selection);
        selectionOverlay.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onPressed);
        selectionOverlay.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onDragged);
        selectionOverlay.addEventHandler(MouseEvent.MOUSE_RELEASED, this::onReleased);
    }

    private void onPressed(MouseEvent e) {
        PageData pageData = viewModel.getPageData().get();
        if (!pageData.isTextExist()) {
            return;
        }

        startX = e.getX();
        startY = e.getY();
        final double fitWidth = viewModel.getFitWidth().doubleValue();

        LOG.debug("onPressed: startX = {}, startY = {}, fitWidth = {}", startX, startY, fitWidth);

        selection.setX(startX);
        selection.setY(startY);
        selection.setWidth(0);
        selection.setHeight(0);
        selection.setVisible(true);
    }

    private GRectangle getRectangle(MouseEvent e) {
        return new GRectangle(startX, startY, e.getX(), e.getY());
    }

    private void setRectangle(MouseEvent e) {
        final GRectangle rectangle = getRectangle(e);

        selection.setX(rectangle.xmin());
        selection.setY(rectangle.ymin());
        selection.setWidth(rectangle.getWidth());
        selection.setHeight(rectangle.getHeight());
    }

    private void onDragged(MouseEvent e) {
        setRectangle(e);
    }

    private void onReleased(MouseEvent e) {
        if (!selection.isVisible() ||
            selection.getWidth() < 2 ||
            selection.getHeight() < 2 ||
            pageView.getImage() == null) {
            selection.setVisible(false);
            return;
        }

        GRectangle rect = getRectangle(e);
        LOG.debug("onReleased: rectangle = {}, pageBox.getWidth() = {}", rect, pageBox.getWidth());
        String text = viewModel.getSelectedText(rect, pageBox.getWidth());

        LOG.debug("text to clipboard = {}", text);
        copyToClipboard(text);

        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(a -> selection.setVisible(false));
        delay.play();
    }

    private void copyToClipboard(String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
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
            LOG.info("File to open: {}", file.getAbsolutePath());
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
                    viewModel, this);
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
        alert.setHeaderText(MainViewModel.APP_NAME);
        alert.setContentText("Version 1.0.0");
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

    @FXML
    private void goToFirstPage() {
        ObservableList<PageNode> pages = pageList.itemsProperty().getValue();
        if (pages == null || pages.isEmpty()) {
            return;
        }

        LOG.debug("Going to the first page with index 0");

        goToPage(0);
    }

    @FXML
    private void goToPrevPage() {
        PageNode selectedPage = pageList.getSelectionModel().getSelectedItem();
        if (selectedPage == null) {
            return;
        }

        int prevIndex = Math.max(selectedPage.getIndex() - 2, 0);
        LOG.debug("Going to previous page with index {}", prevIndex);

        goToPage(prevIndex);
    }

    @FXML
    private void goToNextPage() {
        ObservableList<PageNode> pages = pageList.itemsProperty().getValue();
        if (pages == null || pages.isEmpty()) {
            return;
        }

        PageNode selectedPage = pageList.getSelectionModel().getSelectedItem();
        if (selectedPage == null) {
            return;
        }

        int nextIndex = Math.min(selectedPage.getIndex(), pages.size() - 1);
        LOG.debug("Going to next page with index {}", nextIndex);

        goToPage(nextIndex);
    }

    @FXML
    private void goToLastPage() {
        ObservableList<PageNode> pgs = pageList.itemsProperty().getValue();
        if (pgs == null || pgs.isEmpty()) {
            return;
        }

        int lastIndex = pgs.size() - 1;
        LOG.debug("Going to last page with index {}", lastIndex);

        goToPage(lastIndex);
    }

    @Override
    public void goToPage(int pageIndex) {
        pageSelector.setValue(pageIndex + 1);
        pageList.scrollTo(pageIndex);
        pageList.getSelectionModel().select(pageIndex);
    }
}
