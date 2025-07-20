package com.sternkn.djvu.gui.tree;

import com.sternkn.djvu.file.DjVuFile;
import com.sternkn.djvu.file.chunks.Chunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

public class DjVuTreeModel {
    private static final Logger LOG = LoggerFactory.getLogger(DjVuTreeModel.class);

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

    public void addMouseListener(JTree tree) {
        MouseListener mouseListener = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());

                LOG.debug("selRow = {}, selPath = {}", selRow, selPath);
//                if(selRow != -1) {
//                    if(e.getClickCount() == 1) {
//                        mySingleClick(selRow, selPath);
//                    }
//                    else if(e.getClickCount() == 2) {
//                        myDoubleClick(selRow, selPath);
//                    }
//                }
            }
        };

        tree.addMouseListener(mouseListener);
    }
}
