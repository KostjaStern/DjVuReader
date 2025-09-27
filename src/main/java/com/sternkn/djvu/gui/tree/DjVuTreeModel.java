package com.sternkn.djvu.gui.tree;

import com.sternkn.djvu.file.DjVuFile;
import com.sternkn.djvu.file.chunks.AnnotationChunk;
import com.sternkn.djvu.file.chunks.Chunk;
import com.sternkn.djvu.file.chunks.ChunkId;
import com.sternkn.djvu.file.chunks.DirectoryChunk;
import com.sternkn.djvu.file.chunks.FGbzChunk;
import com.sternkn.djvu.file.chunks.InclChunk;
import com.sternkn.djvu.file.chunks.InfoChunk;
import com.sternkn.djvu.file.chunks.NavmChunk;
import com.sternkn.djvu.file.chunks.TXTzChunk;
import com.sternkn.djvu.file.chunks.TextZone;
import com.sternkn.djvu.file.coders.BufferPointer;
import com.sternkn.djvu.file.coders.GBitmap;
import com.sternkn.djvu.file.coders.GPixmap;
import com.sternkn.djvu.file.coders.IW44Image;
import com.sternkn.djvu.file.coders.JB2CodecDecoder;
import com.sternkn.djvu.file.coders.JB2Dict;
import com.sternkn.djvu.file.coders.JB2Image;
import com.sternkn.djvu.file.coders.PixelColor;
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
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
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

    private static final int[] WHITE = {255, 255, 255, 255}; // Red, Green, Blue, Alpha
    private static final int[] BLACK = {0, 0, 0, 255};
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
        Map<String, Long> compositeChunksStat = this.djvuFile.getChunks().stream()
            .filter(Chunk::isComposite)
            .map(Chunk::getCompositeChunkId)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Map<String, Long> dataChunksStat = this.djvuFile.getChunks().stream()
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
        if (chunk.getChunkId() == ChunkId.Sjbz) {
            return getBitonalImageComponent(chunk);
        }

        if (chunk.getChunkId().isIW44Chunk()) {
            return getBackgroudImageComponent(chunk);
        }

        Chunk decodedChunk = switch (chunk.getChunkId()) {
            case ChunkId.DIRM -> new DirectoryChunk(chunk);
            case ChunkId.INFO -> new InfoChunk(chunk);
            case ChunkId.NAVM -> new NavmChunk(chunk);
            case ChunkId.INCL -> new InclChunk(chunk);
            case ChunkId.FGbz -> new FGbzChunk(chunk);
            case ChunkId.TXTz -> new TXTzChunk(chunk);
            case ChunkId.ANTz, ChunkId.ANTa -> new AnnotationChunk(chunk);
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

    private Component getBackgroudImageComponent(Chunk chunk) {
        List<Chunk> chunks = this.djvuFile.getAllImageChunks(chunk);

        IW44Image image = new IW44Image();
        chunks.forEach(ch -> image.decode_chunk(ch.getData()));
        image.close_codec();

        GPixmap pixmap = image.get_pixmap();

        int height = pixmap.getHeight();
        int width = pixmap.getWidth();
        LOG.debug("IW44 bitmap: height = {}, width = {}", height,  width);

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        WritableRaster raster = img.getRaster();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                PixelColor pixel = pixmap.getPixel(x, y);
                raster.setPixel(x, height - y - 1, pixel.getColor());
            }
        }

        JTextArea textArea = new JTextArea(3, 60);
        textArea.setFont(MONOSPACED_FONT);
        textArea.setText(String.format(
                """
                 %s
                 Bitmap:
                   height = %s
                   width = %s
                """
                , chunk.getDataAsText(), height,  width));
        textArea.setEditable(false);

        JScrollPane topPanel  = new JScrollPane();
        topPanel.setViewportView(textArea);

        JScrollPane bottomPanel = new JScrollPane();
        ImageCanvas imageCanvas = new ImageCanvas(img);
        bottomPanel.setViewportView(imageCanvas);
        imageCanvas.setVisible(true);

        JSplitPane panel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);

        // JPanel panel = new JPanel();
        // GridLayout chunkInfoLayout = new GridLayout(2,1);
        // panel.setLayout(chunkInfoLayout);

        return panel;
    }

    /*
        https://habr.com/ru/articles/331618/ - Smoothing images with Peron and Malik's anisotropic diffusion filter
        Methods of Bitonal Image Conversion for Modern and Classic Documents
     */
    private Component getBitonalImageComponent(Chunk chunk) {
        Chunk sharedShape = this.djvuFile.findSharedShapeChunk(chunk);
        JB2Dict dict = null;
        if (sharedShape != null) {
            dict = new JB2Dict();
            JB2CodecDecoder decoder = new JB2CodecDecoder(new ByteArrayInputStream(sharedShape.getData()));
            decoder.decode(dict);
        }

        JB2Image image = new JB2Image(dict);
        JB2CodecDecoder decoder = new JB2CodecDecoder(new ByteArrayInputStream(chunk.getData()));
        decoder.decode(image);

        GBitmap bitmap = image.get_bitmap();

        int height = bitmap.rows();
        int width = bitmap.columns();
        LOG.debug("bitmap: border = {}, height = {}, width = {}", bitmap.border(), height,  width);

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        WritableRaster raster = img.getRaster();

        for (int rowId = 0; rowId < bitmap.rows(); rowId++) {
            BufferPointer row = bitmap.getRow(rowId);
            for (int colId = 0; colId < bitmap.columns(); colId++) {
                int[] color = row.getValue(colId) == 0 ? WHITE : BLACK;
                raster.setPixel(colId, bitmap.rows() - rowId - 1, color);
            }
        }

        JTextArea textArea = new JTextArea(3, 60);
        textArea.setFont(MONOSPACED_FONT);
        textArea.setText(String.format(
        """
         %s
         Bitmap:
           border = %s
           height = %s
           width = %s
        """
        , chunk.getDataAsText(), bitmap.border(), height,  width));
        textArea.setEditable(false);

        JScrollPane topPanel  = new JScrollPane();
        topPanel.setViewportView(textArea);

        JScrollPane bottomPanel = new JScrollPane();
        ImageCanvas imageCanvas = new ImageCanvas(img);
        bottomPanel.setViewportView(imageCanvas);
        imageCanvas.setVisible(true);

        JSplitPane panel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);

        // JPanel panel = new JPanel();
        // GridLayout chunkInfoLayout = new GridLayout(2,1);
        // panel.setLayout(chunkInfoLayout);

        return panel;
    }

    private static class ImageCanvas extends Canvas {
        BufferedImage image;

        public ImageCanvas(BufferedImage image) {
            this.image = image;
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.scale(0.2, 0.2);

            if (image != null) {
                g.drawImage(image, 40, 10, this); // Draw the image at (0,0)
            }
        }
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
