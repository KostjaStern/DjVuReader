package com.sternkn.djvu.gui;

import com.sternkn.djvu.file.DjVuFile;
import com.sternkn.djvu.file.DjVuFileReader;
import com.sternkn.djvu.file.coders.BufferPointer;
import com.sternkn.djvu.file.coders.GBitmap;
import com.sternkn.djvu.file.coders.JB2CodecDecoder;
import com.sternkn.djvu.file.coders.JB2Dict;
import com.sternkn.djvu.file.coders.JB2Image;
import com.sternkn.djvu.gui.tree.DjVuTreeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.WritableRaster;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainWindow extends Frame {

    private static final Logger LOG = LoggerFactory.getLogger(MainWindow.class);

    private static final int[] WHITE = {255, 255, 255, 255}; // Red, Green, Blue, Alpha
    private static final int[] BLACK = {0, 0, 0, 255};

    private BufferedImage image;
    private DjVuFile djvuFile;
    private JTree tree;
    private ImageCanvas imageCanvas;

    /*
       https://docs.oracle.com/javase/tutorial/uiswing/components/tree.html
     */
    public MainWindow() {
        this.setTitle("DjVu Viewer");
        this.setMenuBar(buildMenuBar());

        // this.image = image;

        this.setLayout(new BorderLayout());

        tree = new JTree();

        tree.setMinimumSize(new Dimension(100, 100));

        // Create a custom canvas to draw the image
        imageCanvas = new ImageCanvas();
        imageCanvas.setMinimumSize(new Dimension(100, 100));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(tree),
                new JScrollPane(imageCanvas));

        this.add(splitPane);

        // Set frame properties
        setSize(600, 300); // Set size to image dimensions
        setLocationRelativeTo(null); // Center the frame
        setVisible(true);
        // Add window listener to handle closing the frame
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                dispose(); // Release resources
                System.exit(0); // Exit the application
            }
        });
    }

    private MenuBar buildMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");

        MenuItem openItem = new MenuItem("Open...");
        openItem.addActionListener(this::openFile);

        fileMenu.add(openItem);
        menuBar.add(fileMenu);

        return menuBar;
    }

    private void openFile(ActionEvent event) {
        FileDialog fileDialog = new FileDialog(this, "Select a file to open", FileDialog.LOAD);
        // fileDialog.setDirectory("C:\\"); // Optional: set initial directory
        fileDialog.setVisible(true); // Show the dialog

        String filename = fileDialog.getFile();
        String directory = fileDialog.getDirectory();

        if (filename == null || directory == null) {
            return;
        }

        final String path = directory + filename;
        LOG.debug("path = {}", path);

        final File file = new File(path);
        try (DjVuFileReader reader = new DjVuFileReader(file)) {
            djvuFile = reader.readFile();
        }

        initTree();
    }

    private void initTree() {
        DjVuTreeModel model = new DjVuTreeModel(this.djvuFile);
        tree.setModel(model.getTreeModel());
    }

    // Custom Canvas class to draw the image
    private class ImageCanvas extends Canvas {
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

    public static void main(String[] args) {
        new MainWindow(); // loadDjVuImage()
    }

    private static BufferedImage getBufferedImage() {
        String imagePath = "./src/main/resources/gerunds_inf.jpg";
        try {
            return ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static BufferedImage loadDjVuImage() {
        File dictionaryFile = new File("./src/test/resources/test_chunks/Djbz_4.data");
        File imageFile = new File("./src/test/resources/test_chunks/Sjbz_16.data");

        JB2Dict dict = new JB2Dict();

        try (InputStream inputStream = new DataInputStream(new FileInputStream(dictionaryFile))) {
            JB2CodecDecoder decoder = new JB2CodecDecoder(inputStream);
            decoder.decode(dict);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        JB2Image image = new JB2Image(dict);

        try (InputStream inputStream = new DataInputStream(new FileInputStream(imageFile))) {
            JB2CodecDecoder decoder = new JB2CodecDecoder(inputStream);
            decoder.decode(image);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        GBitmap bitmap = image.get_bitmap();

        int height = bitmap.rows();
        int width = bitmap.columns();
        System.out.println("bitmap.border() = " + bitmap.border());
        System.out.println("height = " + height);
        System.out.println("width = " + width);

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        WritableRaster raster = img.getRaster();

        for (int rowId = 0; rowId < bitmap.rows(); rowId++) {
            BufferPointer row = bitmap.getRow(rowId);
            for (int colId = 0; colId < bitmap.columns(); colId++) {
                int[] color = row.getValue(colId) == 0 ? WHITE : BLACK;
                raster.setPixel(colId, bitmap.rows() - rowId - 1, color);
            }
        }

        return img;
    }
}
