package com.sternkn.djvu.gui.tree;

import com.sternkn.djvu.file.DjVuFile;
import com.sternkn.djvu.file.chunks.Chunk;
import com.sternkn.djvu.file.chunks.ChunkId;
import com.sternkn.djvu.file.chunks.DirectoryChunk;
import com.sternkn.djvu.file.chunks.FGbzChunk;
import com.sternkn.djvu.file.chunks.InclChunk;
import com.sternkn.djvu.file.chunks.InfoChunk;
import com.sternkn.djvu.file.chunks.NavmChunk;
import com.sternkn.djvu.file.chunks.TXTzChunk;
import com.sternkn.djvu.file.chunks.TextZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.sternkn.djvu.file.utils.StringUtils.NL;
import static com.sternkn.djvu.file.utils.StringUtils.padRight;

public class DjVuTreeModel {
    private static final Logger LOG = LoggerFactory.getLogger(DjVuTreeModel.class);

    private static final Font MONOSPACED_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    private DjVuFile djvuFile;
    private JScrollPane leftPanel;
    private JScrollPane rightPanel;

    public DjVuTreeModel(DjVuFile djvuFile, JScrollPane leftPanel, JScrollPane rightPanel) {
        this.djvuFile = djvuFile;
        this.leftPanel = leftPanel;
        this.rightPanel = rightPanel;
    }

    public void initTree() {
        JTree tree = new JTree();
        tree.setVisible(false);
        tree.setModel(getTreeModel());
        // tree.setMinimumSize(new Dimension(100, 100));

        addMouseListener(tree);
        tree.setVisible(true);
        leftPanel.setViewportView(tree);
    }

    public void initStatistics() {
        JTextArea textArea = new JTextArea(40, 60);
        textArea.setFont(MONOSPACED_FONT);
        textArea.setText(getDjVuChunkStatistics());
        textArea.setEditable(false);
        rightPanel.setViewportView(textArea);
    }

    private String getDjVuChunkStatistics() {
        Map<String, Long> compositeChunksStat = this.djvuFile.chunks().stream()
            .filter(Chunk::isComposite)
            .map(Chunk::getCompositeChunkId)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Map<String, Long> dataChunksStat = this.djvuFile.chunks().stream()
                .filter(c -> !c.isComposite())
                .map(c -> c.getChunkId().name())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        StringBuilder buffer = new StringBuilder();
        buffer.append("    Composite chunks  ").append(NL);
        buffer.append("---------------------------------").append(NL);
        for (Map.Entry<String, Long> entry : compositeChunksStat.entrySet()) {
            buffer.append(" ")
                  .append(padRight(entry.getKey(), 15))
                  .append(": ").append(entry.getValue()).append(NL);
        }
        buffer.append(NL).append(NL);
        buffer.append("    Data chunks  ").append(NL);
        buffer.append("---------------------------------").append(NL);
        for (Map.Entry<String, Long> entry : dataChunksStat.entrySet()) {
            buffer.append(" ")
                  .append(padRight(entry.getKey(), 15))
                  .append(": ").append(entry.getValue()).append(NL);
        }

        return buffer.toString();
    }

    private DefaultTreeModel getTreeModel() {
        List<Chunk> chunks = this.djvuFile.chunks();
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

    private DefaultTreeModel getTreeModelForTextZones(TXTzChunk textChunk) {
        List<TextZone> textZones  = textChunk.getTextZones();
        List<DefaultMutableTreeNode> nodes = new ArrayList<>(textChunk.getTextZoneCount());

        for (TextZone zone : textZones) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TextZoneNode(zone));
            nodes.add(node);
            addTextZoneChildren(node, zone.getChildren());
        }

        return new DefaultTreeModel(nodes.getFirst(), false);
    }

    private void addTextZoneChildren(DefaultMutableTreeNode parent, List<TextZone> textZones) {
        for (TextZone textZone : textZones) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TextZoneNode(textZone));
            parent.add(node);
            addTextZoneChildren(node, textZone.getChildren());
        }
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

    private ChunkTreeNode getSelectedNode(JTree tree, MouseEvent event) {
        TreePath path = tree.getPathForLocation(event.getX(), event.getY());
        if (path == null) {
            return null;
        }

        DefaultMutableTreeNode lastNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        return (ChunkTreeNode) lastNode.getUserObject();
    }

    private void showChunkInfo(JTree tree, MouseEvent event) {
        ChunkTreeNode chunkNode = getSelectedNode(tree, event);
        if (chunkNode == null) {
            return;
        }

        Chunk chunk = chunkNode.getChunk();
        Component component = getChunkComponent(chunk);
        rightPanel.setViewportView(component);
    }

    private Component getChunkComponent(Chunk chunk) {

        Chunk decodedChunk = switch (chunk.getChunkId()) {
            case ChunkId.DIRM -> new DirectoryChunk(chunk);
            case ChunkId.INFO -> new InfoChunk(chunk);
            case ChunkId.NAVM -> new NavmChunk(chunk);
            case ChunkId.INCL -> new InclChunk(chunk);
            case ChunkId.FGbz -> new FGbzChunk(chunk);
            case ChunkId.TXTz -> new TXTzChunk(chunk);
            default -> chunk;
        };

        JTextArea textArea = new JTextArea(20, 60);
        textArea.setFont(MONOSPACED_FONT);
        textArea.setText(decodedChunk.getDataAsText());
        textArea.setEditable(false);

        if (!(decodedChunk instanceof TXTzChunk textChunk)) {
            return textArea;
        }

        JPanel panel = new JPanel();
        panel.add(textArea);

        GridLayout chunkInfoLayout = new GridLayout(2,1);
        panel.setLayout(chunkInfoLayout);

        DefaultTreeModel treeModel = getTreeModelForTextZones(textChunk);

        JTree tree = new JTree();
        tree.setModel(treeModel);
        panel.add(tree);

        return panel;
    }

    private void showPopupMenu(JTree tree, MouseEvent event) {
        ChunkTreeNode chunkNode = getSelectedNode(tree, event);
        if (chunkNode == null) {
            return;
        }

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem saveChunkData = new JMenuItem("Save chunk data as ...");
        saveChunkData.addActionListener(e -> saveChunkDataDialog(chunkNode.getChunk(), tree));
        popupMenu.add(saveChunkData);
        popupMenu.show(event.getComponent(), event.getX(), event.getY());
    }

    private void saveChunkDataDialog(Chunk chunk, JTree tree) {
        LOG.debug("Saving chunk = {} data", chunk);

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save chunk  data as");
        String fileName = String.format("%s_%s.data", chunk.getChunkId().name(), chunk.getId());
        fileChooser.setSelectedFile(new File(fileName));

        int userSelection = fileChooser.showSaveDialog(tree);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                outputStream.write(chunk.getData());
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
