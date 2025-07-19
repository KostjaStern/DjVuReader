package com.sternkn.djvu.gui.tree;

import com.sternkn.djvu.file.DjVuFile;
import com.sternkn.djvu.file.chunks.Chunk;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.List;

public class DjVuTreeModel {

    private DjVuFile djvuFile;

    public DjVuTreeModel(DjVuFile djvuFile) {
        this.djvuFile = djvuFile;
    }

    public DefaultTreeModel getTreeModel() {
        List<Chunk> chunks = this.djvuFile.getChunks();
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
