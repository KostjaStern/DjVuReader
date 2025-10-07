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
    private static final String APP_TITLE = "DjVu Viewer";
    private static final Logger LOG = LoggerFactory.getLogger(MainWindow.class);

    private DjVuFile djvuFile;
    private JScrollPane leftPanel;
    private JSplitPane rightPanel;
    private JToolBar toolBar;

    private DjVuTreeModel model;

    public MainWindow() {
        this.setTitle(APP_TITLE);
        this.setIconImage(getAppIcon());

        this.setMenuBar(buildMenuBar());

        this.toolBar = buildToolBar();
        this.setLayout(new BorderLayout());
        this.add(this.toolBar, BorderLayout.NORTH);

        leftPanel  = new JScrollPane();
        rightPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);

        this.add(splitPane);

        setSize(600, 300);
        setLocationRelativeTo(null);
        setVisible(true);

        // Add window listener to handle closing the frame
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                dispose();
                System.exit(0);
            }
        });
    }

    private JToolBar buildToolBar() {
        JToolBar toolBar = new JToolBar("Still draggable");

        JButton zoomIn = createToolBarButton(ToolBarButton.ZOOM_IN);
        JButton zoomOut = createToolBarButton(ToolBarButton.ZOOM_OUT);

        toolBar.add(zoomIn);
        toolBar.add(zoomOut);

        return toolBar;
    }

    private JButton createToolBarButton(ToolBarButton buttonType) {
        JButton button = new JButton();
        button.setName(buttonType.name());
        button.setActionCommand(buttonType.getActionCommand());
        button.setToolTipText(buttonType.getAltText());

        final String imageLocation = String.format("/icons/%s.png", buttonType.getImageName());
        final URL imageURL = this.getClass().getResource(imageLocation);
        if (imageURL != null) {
            button.setIcon(new ImageIcon(imageURL, buttonType.getAltText()));
        } else {
            button.setText(buttonType.getAltText());
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

        Menu viewMenu = new Menu("View");
        MenuItem showStatisticsItem = new MenuItem("Show statistics");
        showStatisticsItem.addActionListener((l) -> {
            if (this.model != null) {
                this.model.initStatistics();
            }
        });
        viewMenu.add(showStatisticsItem);
        menuBar.add(viewMenu);

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

        model = new DjVuTreeModel(djvuFile, leftPanel,  rightPanel, toolBar);
        model.initTree();
        model.initStatistics();
    }

    public static void main(String[] args) {
        System.setProperty("apple.awt.application.name", APP_TITLE);
        if (Taskbar.isTaskbarSupported()) {
            Taskbar tb = Taskbar.getTaskbar();
            if (tb.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                tb.setIconImage(getAppIcon());
            }
        }
        new MainWindow();
    }

    private static Image getAppIcon() {
        URL appImageURL = MainWindow.class.getResource("/icons/djvu_app_icon_2_128.png");
        return Toolkit.getDefaultToolkit().getImage(appImageURL);
    }
}
