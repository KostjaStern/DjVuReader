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
import com.sternkn.djvu.file.coders.IW44SecondaryHeader;
import com.sternkn.djvu.file.coders.JB2CodecDecoder;
import com.sternkn.djvu.file.coders.JB2Dict;
import com.sternkn.djvu.file.coders.JB2Image;
import com.sternkn.djvu.file.coders.PixelColor;
import com.sternkn.djvu.gui.ImageCanvas;
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
    private JSplitPane rightPanel;
    private JToolBar toolBar;

    public DjVuTreeModel(DjVuFile djvuFile, JScrollPane leftPanel, JSplitPane rightPanel, JToolBar toolBar) {
        this.djvuFile = djvuFile;
        this.leftPanel = leftPanel;
        this.rightPanel = rightPanel;
        this.toolBar = toolBar;
    }

    public void initTree() {
        JTree tree = new JTree();
        tree.setModel(getTreeModel());
        addMouseListener(tree);

        leftPanel.setViewportView(tree);
    }

    public void initStatistics() {
        addTopTextInfo(getDjVuChunkStatistics(), 40, 60);
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

        showChunk(chunkNode.getChunk());
    }

    private void showChunk(Chunk chunk) {
        if (chunk.getChunkId() == ChunkId.Sjbz) {
            showBitonalChunkImage(chunk);
        }

        if (chunk.getChunkId().isIW44Chunk()) {
            showBackgroudChunkImage(chunk);
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

        addTopTextInfo(decodedChunk.getDataAsText(), 20, 60);

        if (!(decodedChunk instanceof TXTzChunk textChunk)) {
            return;
        }

        DefaultTreeModel treeModel = getTreeModelForTextZones(textChunk);

        JTree tree = new JTree();
        tree.setModel(treeModel);
        this.rightPanel.setBottomComponent(new JScrollPane(tree));
    }

    private void addTopTextInfo(String text, int rows, int columns) {
        JTextArea textArea = new JTextArea(rows, columns);
        textArea.setFont(MONOSPACED_FONT);
        textArea.setText(text);
        textArea.setEditable(false);

        JScrollPane scroll = new JScrollPane(textArea);
        this.rightPanel.setTopComponent(scroll);
    }

    private void showBackgroudChunkImage(Chunk chunk) {
        List<Chunk> chunks = this.djvuFile.getAllImageChunks(chunk);

        IW44Image image = new IW44Image();
        chunks.forEach(ch -> image.decode_chunk(ch.getData()));
        image.close_codec();
        IW44SecondaryHeader header = image.getSecondaryHeader();

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

        String text = String.format(
                """
                 %s
                 majorVersion = %s
                 minorVersion = %s
                 colorType = %s
                 chrominanceDelay = %s
                 crcbHalf = %s
                 height = %s
                 width = %s
                """, chunk.getDataAsText(),
                header.getMajorVersion(), header.getMinorVersion(), header.getColorType(),
                header.getChrominanceDelay(), header.getCrcbHalf(),
                height,  width);

        addTopTextInfo(text, 3, 60);

        ImageCanvas imageCanvas = new ImageCanvas(img, toolBar);
        JScrollPane bottomPanel = new JScrollPane(imageCanvas);
        imageCanvas.setVisible(true);
        this.rightPanel.setBottomComponent(bottomPanel);
    }

    /*
        https://habr.com/ru/articles/331618/ - Smoothing images with Peron and Malik's anisotropic diffusion filter
        Methods of Bitonal Image Conversion for Modern and Classic Documents
     */
    private void showBitonalChunkImage(Chunk chunk) {
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

        String text = String.format(
        """
         %s
         Bitmap:
           border = %s
           height = %s
           width = %s
        """, chunk.getDataAsText(), bitmap.border(), height,  width);

        addTopTextInfo(text, 3, 60);

        ImageCanvas imageCanvas = new ImageCanvas(img, toolBar);
        JScrollPane bottomPanel = new JScrollPane(imageCanvas);
        imageCanvas.setVisible(true);
        this.rightPanel.setBottomComponent(bottomPanel);
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
