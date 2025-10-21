package com.sternkn.djvu.gui.view_model;

import com.sternkn.djvu.file.DjVuFile;
import com.sternkn.djvu.file.DjVuFileReader;
import com.sternkn.djvu.file.chunks.Chunk;
import com.sternkn.djvu.model.DjVuModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DjVuFileWorker extends SwingWorker<DjVuFile, Void> {
    private static final Logger LOG = LoggerFactory.getLogger(DjVuFileWorker.class);

    private final File file;
    private final MainViewModel viewModel;

    public DjVuFileWorker(MainViewModel viewModel, File file) {
        this.viewModel = viewModel;
        this.file = file;
    }

    @Override
    public DjVuFile doInBackground() {
        try (DjVuFileReader reader = new DjVuFileReader(file)) {
            return reader.readFile();
        }
    }

    @Override
    public void done() {
        viewModel.setBusy(false);
        try {
            DjVuFile djvFile = get();
            DefaultTreeModel treeModel = getTreeModel(djvFile);
            viewModel.setTreeModel(treeModel);

            DjVuModel djvuModel = new DjVuModel(djvFile);
            viewModel.setDjvuModel(djvuModel);

            viewModel.setTitle(file.getName());
        }
        catch (InterruptedException | ExecutionException exception) {
            LOG.error("File reading error - {}", exception.getMessage());
            viewModel.setErrorMessage(exception.getMessage());
        }
    }

    private DefaultTreeModel getTreeModel(DjVuFile djvuFile) {
        List<Chunk> chunks = djvuFile.getChunks();
        DefaultMutableTreeNode[] nodes = new DefaultMutableTreeNode[chunks.size()];

        int index = 0;
        for (Chunk chunk : chunks) {
            DefaultMutableTreeNode  node = new DefaultMutableTreeNode(new ChunkTreeNode(chunk));
            nodes[index] = node;

            Chunk parentChunk = chunk.getParent();
            if (parentChunk != null) {
                DefaultMutableTreeNode parentNode = nodes[(int) parentChunk.getId()];
                parentNode.add(node);
            }
            index++;
        }

        return new DefaultTreeModel(nodes[0], false);
    }
}
