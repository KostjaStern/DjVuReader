package com.sternkn.djvu.gui.view_model;

import com.sternkn.djvu.model.DjVuModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.DefaultTreeModel;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;

public class MainViewModel {
    private static final Logger LOG = LoggerFactory.getLogger(MainViewModel.class);

    public static final String APP_TITLE = "DjVu Viewer";

    private final PropertyChangeSupport propertyChange;

    private String title;
    private boolean busy;
    private DefaultTreeModel treeModel;
    private DjVuModel djvuModel;

    private String topText;
    private DefaultTreeModel textTreeModel;
    private BufferedImage image;
    private String errorMessage;

    public MainViewModel() {
        propertyChange = new PropertyChangeSupport(this);
        title = APP_TITLE;
        errorMessage  = "";
        topText = "";
    }

    public void showStatistics() {
        this.setTopText(djvuModel.getChunkStatistics());
    }

    public void loadFileAsync(File file) {
        setBusy(true);

        DjVuFileWorker worker = new DjVuFileWorker(this, file);
        worker.execute();
    }

    public void showChunkInfo(long chunkId) {
        setBusy(true);

        ChunkDecodingWorker worker = new ChunkDecodingWorker(this, djvuModel, chunkId);
        worker.execute();
    }

    public void saveChunkData(File file, long chunkId) {
        djvuModel.saveChunkData(file, chunkId);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        var old = this.title;
        this.title = title;
        firePropertyChange(FieldName.TITLE, old, title);
    }

    public String getTopText() {
        return topText;
    }

    public void setTopText(String topText) {
        var old = this.topText;
        this.topText = topText;
        firePropertyChange(FieldName.TOP_TEXT, old, topText);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        var old = this.errorMessage;
        this.errorMessage = errorMessage;
        firePropertyChange(FieldName.ERROR_MESSAGE, old, errorMessage);
    }

    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }

    public void setTreeModel(DefaultTreeModel treeModel) {
        var old = this.treeModel;
        this.treeModel = treeModel;
        firePropertyChange(FieldName.TREE_MODEL, old, treeModel);
    }

    public DefaultTreeModel getTextTreeModel() {
        return textTreeModel;
    }
    public void setTextTreeModel(DefaultTreeModel textTreeModel) {
        var old = this.textTreeModel;
        this.textTreeModel = textTreeModel;
        firePropertyChange(FieldName.TEXT_TREE_MODEL, old, textTreeModel);
    }

    public BufferedImage getImage() {
        return image;
    }
    public void setImage(BufferedImage image) {
        var old = this.image;
        this.image = image;
        firePropertyChange(FieldName.IMAGE, old, image);
    }

    public DjVuModel getDjvuModel() {
        return djvuModel;
    }

    public void setDjvuModel(DjVuModel djvuModel) {
        var old = this.djvuModel;
        this.djvuModel = djvuModel;
        firePropertyChange(FieldName.DJVU_MODEL, old, djvuModel);
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        var old = this.busy;
        this.busy = busy;
        firePropertyChange(FieldName.BUSY, old, busy);
    }

    private void firePropertyChange(FieldName fieldName, Object oldValue, Object newValue) {
        propertyChange.firePropertyChange(fieldName.name(), oldValue, newValue);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChange.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChange.removePropertyChangeListener(listener);
    }
}
