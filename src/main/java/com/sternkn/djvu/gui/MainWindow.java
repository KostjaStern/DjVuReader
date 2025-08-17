package com.sternkn.djvu.gui;

import com.sternkn.djvu.file.DjVuFile;
import com.sternkn.djvu.file.DjVuFileReader;
import com.sternkn.djvu.gui.tree.DjVuTreeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;

public class MainWindow extends Frame {

    private static final Logger LOG = LoggerFactory.getLogger(MainWindow.class);

    private DjVuFile djvuFile;
    private JScrollPane leftPanel;
    private JScrollPane rightPanel;

    /*
       https://docs.oracle.com/javase/tutorial/uiswing/components/tree.html
     */
    public MainWindow() {
        this.setTitle("DjVu Viewer");
        this.setMenuBar(buildMenuBar());

        this.setLayout(new BorderLayout());
        this.add(buildToolBar(), BorderLayout.NORTH);

        leftPanel  = new JScrollPane();
        rightPanel = new JScrollPane();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);

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

    private JToolBar buildToolBar() {
        JToolBar toolBar = new JToolBar("Still draggable");

        JButton zoomIn = createToolBarButton("zoom-in-32", "previous", "zoom in");
        JButton zoomOut = createToolBarButton("zoom-out-32", "up", "zoom out");

        toolBar.add(zoomIn);
        toolBar.add(zoomOut);

        return toolBar;
    }

    private JButton createToolBarButton(String imageName,
                                           String actionCommand,
                                           String altText) {
        JButton button = new JButton();
        button.setActionCommand(actionCommand);
        button.setToolTipText(altText);
        // button.addActionListener(this);

        final String imageLocation = String.format("/icons/%s.png", imageName);
        final URL imageURL = this.getClass().getResource(imageLocation);
        if (imageURL != null) {
            button.setIcon(new ImageIcon(imageURL, altText));
        } else {
            button.setText(altText);
            LOG.error("Resource not found: {}", imageLocation);
        }

        return button;
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
        fileDialog.setVisible(true);

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

        DjVuTreeModel model = new DjVuTreeModel(djvuFile, leftPanel,  rightPanel);
        model.initTree();
        model.initStatistics();
    }

    public static void main(String[] args) {
        new MainWindow();
    }
}
