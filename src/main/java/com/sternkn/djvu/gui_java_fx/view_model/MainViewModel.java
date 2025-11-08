package com.sternkn.djvu.gui_java_fx.view_model;

import com.sternkn.djvu.file.DjVuFile;
import com.sternkn.djvu.file.chunks.Chunk;
import com.sternkn.djvu.file.chunks.TextZone;
import com.sternkn.djvu.file.coders.PixelColor;
import com.sternkn.djvu.file.coders.Pixmap;
import com.sternkn.djvu.gui.view_model.ChunkTreeNode;
import com.sternkn.djvu.gui.view_model.TextZoneNode;
import com.sternkn.djvu.model.ChunkInfo;
import com.sternkn.djvu.model.DjVuModel;
import com.sternkn.djvu.model.DjVuModelImpl;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainViewModel {
    private static final Logger LOG = LoggerFactory.getLogger(MainViewModel.class);

    public static final String APP_TITLE = "DjVu Viewer";
    public static final double zoomDelta = 0.01;

    // private final PropertyChangeSupport propertyChange;
    private final FileTaskFactory fileTaskFactory;
    private final ChunkDecodingTaskFactory chunkDecodingTaskFactory;

    private DjVuModel djvuModel;

    // main window title
    private StringProperty title;

    // This flag indicates that some long calculation/loading is in progress.
    private DoubleProperty progress;
    // public static final double INDETERMINATE_PROGRESS = (double)-1.0F;

    private StringProperty zoom;

    // left chunk tree
    private ObjectProperty<TreeItem<ChunkTreeNode>> chunkRootNode;

    // controls on right panel
    private StringProperty topText;
    private ObjectProperty<TreeItem<TextZoneNode>> textRootNode;
    private BooleanProperty showTextTree;
    private ObjectProperty<Image> image;

    // the latest error message
    private StringProperty errorMessage;

    public MainViewModel() {
        this(DjVuFileTask::new, ChunkDecodingTask::new);
    }

    public MainViewModel(FileTaskFactory fileTaskFactory,
                         ChunkDecodingTaskFactory chunkDecodingTaskFactory) {
        this.fileTaskFactory = fileTaskFactory;
        this.chunkDecodingTaskFactory = chunkDecodingTaskFactory;

        title = new SimpleStringProperty(APP_TITLE);
        errorMessage  = new SimpleStringProperty("");
        topText = new SimpleStringProperty("");

        textRootNode = new SimpleObjectProperty<>();
        showTextTree  = new SimpleBooleanProperty(false);
        chunkRootNode = new SimpleObjectProperty<>();
        image = new SimpleObjectProperty<>();
        progress = new SimpleDoubleProperty(0);
        zoom = new SimpleStringProperty("1.0");
    }

    public void showStatistics() {
        this.setTopText(djvuModel.getChunkStatistics());
    }

    public void loadFileAsync(File file) {
        setInProgress();

        Task<DjVuFile> task = fileTaskFactory.create(file);
        task.setOnSucceeded(event -> {
            DjVuFile djvFile = task.getValue();
            TreeItem<ChunkTreeNode> rootNode = getRootNode(djvFile);

            setChunkRootNode(rootNode);
            setDjvuModel(new DjVuModelImpl(djvFile));
            setTitle(file.getName());
            setProgressDone();
        });

        task.setOnFailed(event -> {
            setErrorMessage(task.getException().getMessage());
            setProgressDone();
        });

        new Thread(task).start();
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

        Task<ChunkInfo> task = chunkDecodingTaskFactory.create(djvuModel, chunkId);
        task.setOnSucceeded(event -> {
            ChunkInfo chunkInfo = task.getValue();

            TreeItem<TextZoneNode> textRootNode = getTextRootNode(chunkInfo, chunkId);
            setTextRootNode(textRootNode);
            setShowTextTree(textRootNode != null);
            setTopText(chunkInfo.getTextData());
            setImage(getImage(chunkInfo.getBitmap()));
            setProgressDone();
        });

        task.setOnFailed(event -> {
            setErrorMessage(task.getException().getMessage());
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

    private Image getImage(Pixmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        LOG.debug("bitmap: border = {}, height = {}, width = {}", bitmap.getBorder(), height,  width);


        WritableImage image = new WritableImage(width, height);
        PixelWriter pixelWriter = image.getPixelWriter();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                PixelColor pixel = bitmap.getPixel(x, y);
                Color color = Color.rgb(pixel.getRed(), pixel.getGreen(), pixel.getBlue());
                pixelWriter.setColor(x, height - y - 1, color);
            }
        }

        return image;
    }

    public void saveChunkData(File file, long chunkId) {
        djvuModel.saveChunkData(file, chunkId);
    }

    public StringProperty getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title.set(title);
    }

    public StringProperty getZoom() {
        return zoom;
    }
    public void zoomIn() {
        double currentZoom = Double.parseDouble(zoom.get());
        double newZoom = currentZoom + zoomDelta;
        zoom.set(String.valueOf(newZoom));
    }
    public void zoomOut() {
        double currentZoom = Double.parseDouble(zoom.get());
        double newZoom = currentZoom - zoomDelta;
        zoom.set(String.valueOf(newZoom));
    }

    public StringProperty getTopText() {
        return topText;
    }

    public void setTopText(String topText) {
        this.topText.set(topText);
    }

    public StringProperty getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage.set(errorMessage);
    }

    public ObjectProperty<TreeItem<ChunkTreeNode>> getChunkRootNode() {
        return chunkRootNode;
    }
    public void setChunkRootNode(TreeItem<ChunkTreeNode> rootNode) {
        chunkRootNode.set(rootNode);
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

    public void setDjvuModel(DjVuModel djvuModel) {
        this.djvuModel = djvuModel;
    }

    public DoubleProperty getProgress() {
        return progress;
    }
    public void setInProgress() {
        this.progress.set(ProgressBar.INDETERMINATE_PROGRESS);
    }
    public void setProgressDone() {
        this.progress.set(0);
    }
}
