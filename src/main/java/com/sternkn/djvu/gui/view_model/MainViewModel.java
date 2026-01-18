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

import com.sternkn.djvu.file.DjVuFile;
import com.sternkn.djvu.file.chunks.Chunk;
import com.sternkn.djvu.file.chunks.ImageRotationType;
import com.sternkn.djvu.file.chunks.TextZone;
import com.sternkn.djvu.model.ChunkInfo;
import com.sternkn.djvu.model.DjVuModel;
import com.sternkn.djvu.model.DjVuModelImpl;
import com.sternkn.djvu.model.MenuNode;
import com.sternkn.djvu.model.Page;
import com.sternkn.djvu.model.PageCache;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.sternkn.djvu.utils.ExceptionUtils.getStackTraceAsString;
import static com.sternkn.djvu.utils.ImageUtils.toImage;

public class MainViewModel {
    private static final Logger LOG = LoggerFactory.getLogger(MainViewModel.class);

    public static final String APP_TITLE = "DjVu Viewer";
    public static final int ZOOM_DELTA = 10;

    private final FileTaskFactory fileTaskFactory;
    private final ChunkDecodingTaskFactory chunkDecodingTaskFactory;
    // private final PageLoadingTaskFactory pageLoadingTaskFactory;
    private final ThumbnailLoadingTaskFactory thumbnailLoadingTaskFactory;

    private DjVuModel djvuModel;

    private final StringProperty title;

    private final StringProperty progressMessage;

    private final DoubleProperty progress;

    private final DoubleProperty fitWidth;

    private final ListProperty<PageNode> pages;

    // left chunk tree
    private final ObjectProperty<TreeItem<ChunkTreeNode>> chunkRootNode;

    private final ObjectProperty<TreeItem<MenuNode>> menuRootNode;

    // controls on right panel
    private final StringProperty topText;
    private final ObjectProperty<TreeItem<TextZoneNode>> textRootNode;
    private final BooleanProperty showTextTree;
    private final BooleanProperty disableNavigationMenu;
    private final BooleanProperty disableStatisticsMenu;
    private final ObjectProperty<Image> image;
    private final ObjectProperty<Image> pageImage;

    private final ExecutorService decodePool;
    private PageCache pageCache;

    private Task<Void> thumbnailLoadingTask;

    public MainViewModel() {
        this(DjVuFileTask::new, ChunkDecodingTask::new, ThumbnailLoadingTask::new); // PageLoadingTask::new
    }

    public MainViewModel(FileTaskFactory fileTaskFactory,
                         ChunkDecodingTaskFactory chunkDecodingTaskFactory,
                         // PageLoadingTaskFactory pageLoadingTaskFactory,
                         ThumbnailLoadingTaskFactory thumbnailLoadingTaskFactory) {

        decodePool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        pageCache = null;

        this.fileTaskFactory = fileTaskFactory;
        this.chunkDecodingTaskFactory = chunkDecodingTaskFactory;
        // this.pageLoadingTaskFactory = pageLoadingTaskFactory;
        this.thumbnailLoadingTaskFactory = thumbnailLoadingTaskFactory;

        title = new SimpleStringProperty(APP_TITLE);
        progressMessage = new SimpleStringProperty("");
        topText = new SimpleStringProperty("");

        pages = new SimpleListProperty<>();
        textRootNode = new SimpleObjectProperty<>();
        showTextTree = new SimpleBooleanProperty(false);
        disableNavigationMenu = new SimpleBooleanProperty(true);
        disableStatisticsMenu = new SimpleBooleanProperty(true);
        chunkRootNode = new SimpleObjectProperty<>();
        menuRootNode = new SimpleObjectProperty<>();
        image = new SimpleObjectProperty<>();
        pageImage = new SimpleObjectProperty<>();
        progress = new SimpleDoubleProperty(0);
        fitWidth = new SimpleDoubleProperty(-1);
    }

    private void resetState() {
        if (thumbnailLoadingTask != null && thumbnailLoadingTask.isRunning()) {
            LOG.debug("We are cancelling the thumbnail loading task ...");
            thumbnailLoadingTask.cancel();
        }

        title.setValue(APP_TITLE);
        progressMessage.setValue("");
        topText.setValue("");

        pages.setValue(null);
        textRootNode.setValue(null);
        showTextTree.setValue(false);
        chunkRootNode.setValue(null);
        menuRootNode.setValue(null);
        image.setValue(null);
        pageImage.setValue(null);
    }

    public void showStatistics() {
        this.setTopText(djvuModel.getChunkStatistics());
    }

    public void loadFileAsync(File file) {
        resetState();
        setInProgress();
        setProgressMessage("Loading " + file.getName() + " ...");

        Task<DjVuFile> task = fileTaskFactory.create(file);
        task.setOnSucceeded(event -> {
            DjVuFile djvFile = task.getValue();

            TreeItem<ChunkTreeNode> rootNode = getRootNode(djvFile);
            setChunkRootNode(rootNode);

            DjVuModelImpl djvuModel = new DjVuModelImpl(djvFile);
            setDjvuModel(djvuModel);
            this.pageCache = new PageCache(djvuModel, decodePool);

            boolean isMenuEmpty = djvuModel.getMenuNodes().isEmpty();
            setDisableNavigationMenu(isMenuEmpty);
            setDisableStatisticsMenu(false);

            if (!isMenuEmpty) {
                MenuNode rootMenuNode = djvuModel.getMenuNodes().getFirst();
                setMenuRootNode(new MenuTreeItem(rootMenuNode));
            }


            setPages(djvuModel.getPages());

            setTitle(file.getName());

            setProgressMessage("");
            setProgressDone();
            loadingPageThumbnails();
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            LOG.error(getStackTraceAsString(exception));

            setProgressMessage(exception.getMessage());
            setProgressDone();
        });

        new Thread(task).start();
    }

    public void loadPageAsync(PageNode page) {
        LOG.info("Loading page {}", page);
        setInProgress();
        setProgressMessage("Loading page ...");

        pageCache.get(page.getPage()).whenComplete((data, exception) -> {
            if (exception != null) {
                Platform.runLater(() -> {
                    LOG.error(getStackTraceAsString(exception));
                    setProgressDone();

                    String errorMessage = exception.getMessage();
                    setProgressMessage(errorMessage);
                });
                return;
            }

            Platform.runLater(() -> {
                Image image = data.image();

                setProgressMessage("");

                setPageImage(image);
                setProgressDone();
            });
        });
/*
        Task<Image> task = pageLoadingTaskFactory.create(djvuModel, page.getPage());
        task.setOnSucceeded(event -> {
            Image image = task.getValue();
            setProgressMessage("");

            setPageImage(image);
            setProgressDone();
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            LOG.error(getStackTraceAsString(exception));

            String errorMessage = exception.getMessage();
            setProgressMessage(errorMessage);
            setProgressDone();
        });

        new Thread(task).start();
 */
    }

    private TreeItem<ChunkTreeNode> getRootNode(DjVuFile djvuFile) {
        List<Chunk> chunks = djvuFile.getChunks();

        TreeItem<ChunkTreeNode> root = null;
        List<TreeItem<ChunkTreeNode>> nodes = new ArrayList<>(chunks.size());

        for (Chunk chunk : chunks) {
            TreeItem<ChunkTreeNode> node = new TreeItem<>(new ChunkTreeNode(chunk));
            if (root == null) {
                root = node;
            }
            nodes.add(node);

            Chunk parentChunk = chunk.getParent();
            if (parentChunk != null) {
                TreeItem<ChunkTreeNode> parentNode = nodes.get((int) parentChunk.getId());
                parentNode.getChildren().add(node);
            }
        }

        return root;
    }

    public void showChunkInfo(long chunkId) {
        setInProgress();
        setProgressMessage("Decoding chunk ...");

        Task<ChunkInfo> task = chunkDecodingTaskFactory.create(djvuModel, chunkId);
        task.setOnSucceeded(event -> {
            setProgressMessage("");
            ChunkInfo chunkInfo = task.getValue();

            TreeItem<TextZoneNode> textRootNode = getTextRootNode(chunkInfo, chunkId);
            setTextRootNode(textRootNode);
            setShowTextTree(textRootNode != null);
            setTopText(chunkInfo.getTextData());
            setImage(toImage(chunkInfo.getBitmap(), ImageRotationType.UPSIDE_DOWN));
            setProgressDone();
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            LOG.error(getStackTraceAsString(exception));

            setProgressMessage(exception.getMessage());
            setProgressDone();
        });

        new Thread(task).start();
    }

    private TreeItem<TextZoneNode> getTextRootNode(ChunkInfo chunkInfo, long chunkId) {
        List<TextZone> textZones  = chunkInfo.getTextZones();
        if (textZones == null || textZones.isEmpty()) {
            return null;
        }

        final int size = textZones.size();
        if (size > 1) {
            LOG.warn("More than one root text zone {} for chunkId = {}", size, chunkId);
        }

        TextZone root = textZones.getFirst();
        TreeItem<TextZoneNode> rootNode = new TreeItem<>(new TextZoneNode(root));

        for (TextZone zone : root.getChildren()) {
            TreeItem<TextZoneNode> node = new TreeItem<>(new TextZoneNode(zone));
            rootNode.getChildren().add(node);

            addTextZoneChildren(node, zone.getChildren());
        }

        return rootNode;
    }

    private void addTextZoneChildren(TreeItem<TextZoneNode> parent, List<TextZone> textZones) {
        for (TextZone textZone : textZones) {
            TreeItem<TextZoneNode> node = new TreeItem<>(new TextZoneNode(textZone));
            parent.getChildren().add(node);
            addTextZoneChildren(node, textZone.getChildren());
        }
    }

    public void saveChunkData(File file, long chunkId) {
        djvuModel.saveChunkData(file, chunkId);
    }

    public ListProperty<PageNode> getPages() {
        return pages;
    }
    public void setPages(List<Page> p) {
        AtomicInteger idx = new AtomicInteger(1);
        List<PageNode> pgs = p.stream()
            .map(pg -> new PageNode(pg, idx.getAndIncrement()))
            .toList();

        var list = FXCollections.observableList(pgs);
        pages.setValue(list);
    }

    public void loadingPageThumbnails() {
        LOG.debug("Loading page thumbnails ...");

        thumbnailLoadingTask = this.thumbnailLoadingTaskFactory.create(this, djvuModel);

        thumbnailLoadingTask.setOnFailed(e -> {
            Throwable exception = thumbnailLoadingTask.getException();
            LOG.error(getStackTraceAsString(exception));

            setProgressDone();
            setProgressMessage(exception.getMessage());
        });

        new Thread(thumbnailLoadingTask).start();
    }

    public StringProperty getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title.set(title);
    }

    public DoubleProperty getFitWidth() {
        return this.fitWidth;
    }
    public void zoomIn() {
        double width = this.fitWidth.get();
        if (width < 0) {
            return;
        }

        double newWidth = width + ZOOM_DELTA;
        fitWidth.set(newWidth);
    }
    public void zoomOut() {
        double width = this.fitWidth.get();
        double newWidth = width - ZOOM_DELTA;
        if (newWidth < 0) {
            return;
        }

        fitWidth.set(newWidth);
    }

    public StringProperty getTopText() {
        return topText;
    }

    public void setTopText(String topText) {
        this.topText.set(topText);
    }

    public StringProperty getProgressMessage() {
        return progressMessage;
    }

    public void setProgressMessage(String progressMessage) {
        this.progressMessage.set(progressMessage);
    }

    public ObjectProperty<TreeItem<ChunkTreeNode>> getChunkRootNode() {
        return chunkRootNode;
    }
    public void setChunkRootNode(TreeItem<ChunkTreeNode> rootNode) {
        chunkRootNode.set(rootNode);
    }

    public ObjectProperty<TreeItem<MenuNode>> getMenuRootNode() {
        return menuRootNode;
    }
    public void setMenuRootNode(TreeItem<MenuNode> rootNode) {
        menuRootNode.set(rootNode);
    }

    public BooleanProperty disableNavigationMenu() {
        return disableNavigationMenu;
    }
    public void setDisableNavigationMenu(Boolean value) {
        this.disableNavigationMenu.set(value);
    }

    public BooleanProperty disableStatisticsMenu() {
        return disableStatisticsMenu;
    }
    public void setDisableStatisticsMenu(Boolean value) {
        disableStatisticsMenu.set(value);
    }

    public BooleanProperty getShowTextTree() {
        return showTextTree;
    }
    public void setShowTextTree(Boolean value) {
        this.showTextTree.set(value);
    }

    public ObjectProperty<TreeItem<TextZoneNode>> getTextRootNode() {
        return this.textRootNode;
    }
    public void setTextRootNode(TreeItem<TextZoneNode> rootNode) {
        this.textRootNode.set(rootNode);
    }

    public ObjectProperty<Image> getImage() {
        return image;
    }
    public void setImage(Image img) {
        this.image.set(img);
    }

    public ObjectProperty<Image> getPageImage() {
        return pageImage;
    }
    public void setPageImage(Image pageImage) {
        this.pageImage.set(pageImage);
    }

    public void setDjvuModel(DjVuModel djvuModel) {
        this.djvuModel = djvuModel;
    }

    public DoubleProperty getProgress() {
        return progress;
    }
    public void setInProgress() {
        this.progress.set(ProgressBar.INDETERMINATE_PROGRESS);
    }
    public void setProgress(double value) {
        this.progress.set(value);
    }
    public void setProgressDone() {
        this.progress.set(0);
    }
}
