package com.sternkn.djvu.gui.view;

import com.sternkn.djvu.gui.view_model.ChunkTreeNode;
import com.sternkn.djvu.gui.view_model.MainViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JTree;
import javax.swing.JScrollPane;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JFileChooser;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import static com.sternkn.djvu.gui.view.ControlName.FILE_CHUNKS_TREE;
import static com.sternkn.djvu.gui.view.ControlName.SAVE_CHUNK_DATA_DIALOG;

public class ChunkTree {
    private static final Logger LOG = LoggerFactory.getLogger(ChunkTree.class);

    private JTree tree;
    private final JScrollPane panel;
    private final MainViewModel viewModel;

    public ChunkTree(JScrollPane panel, MainViewModel viewModel) {
        this.panel = panel;
        this.viewModel = viewModel;
        initTree();
    }

    public void setModel(DefaultTreeModel model) {
        tree.setModel(model);
        tree.setVisible(true);
    }

    private void initTree() {
        tree = new JTree();
        tree.setName(FILE_CHUNKS_TREE.name());
        tree.setVisible(false);
        addMouseListener(tree);
        panel.setViewportView(tree);
    }

    private void addMouseListener(JTree tree) {
        MouseListener mouseListener = new MouseAdapter() {
            public void mousePressed(MouseEvent event) {
                if (event.isPopupTrigger()) {
                    showPopupMenu(tree, event);
                }
                else {
                    showChunkInfo(tree, event);
                }
            }
        };

        tree.addMouseListener(mouseListener);
    }

    private void showChunkInfo(JTree tree, MouseEvent event) {
        ChunkTreeNode chunkNode = getSelectedNode(tree, event);
        if (chunkNode == null) {
            return;
        }

        viewModel.showChunkInfo(chunkNode.getChunkId());
    }

    private void showPopupMenu(JTree tree, MouseEvent event) {
        ChunkTreeNode chunkNode = getSelectedNode(tree, event);
        if (chunkNode == null) {
            return;
        }

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem saveChunkData = new JMenuItem("Save chunk data as ...");
        saveChunkData.addActionListener(e -> saveChunkDataDialog(chunkNode, tree));
        popupMenu.add(saveChunkData);
        popupMenu.show(event.getComponent(), event.getX(), event.getY());
    }

    private ChunkTreeNode getSelectedNode(JTree tree, MouseEvent event) {
        TreePath path = tree.getPathForLocation(event.getX(), event.getY());
        if (path == null) {
            return null;
        }

        DefaultMutableTreeNode lastNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        return (ChunkTreeNode) lastNode.getUserObject();
    }

    private void saveChunkDataDialog(ChunkTreeNode chunkNode, JTree tree) {
        LOG.debug("Saving chunk = {} data", chunkNode);

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setName(SAVE_CHUNK_DATA_DIALOG.name());
        fileChooser.setDialogTitle("Save chunk  data as");
        String fileName = String.format("%s_%s.data", chunkNode.getChunkName(), chunkNode.getChunkId());
        fileChooser.setSelectedFile(new File(fileName));

        int userSelection = fileChooser.showSaveDialog(tree);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            this.viewModel.saveChunkData(file, chunkNode.getChunkId());
        }
    }
}
