package com.sternkn.djvu.gui.view;

import com.sternkn.djvu.gui.view_model.FieldName;
import com.sternkn.djvu.gui.view_model.MainViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.net.URL;


public class MainFrame extends JFrame {

    private static final Logger LOG = LoggerFactory.getLogger(MainFrame.class);

    private static final Font MONOSPACED_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    private ChunkTree chunkTree;
    private JScrollPane leftPanel;

    private JTextArea topTextArea;
    private JTree textTree;

    private ImageCanvas imageCanvas;
    private JSplitPane rightPanel;

    private JToolBar toolBar;

    private final MainViewModel viewModel;

    static {
        if (Taskbar.isTaskbarSupported()) {
            Taskbar tb = Taskbar.getTaskbar();
            if (tb.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                tb.setIconImage(getAppIcon());
            }
        }
    }

    public MainFrame(MainViewModel viewModel) {
        this.viewModel = viewModel;

        this.setTitle(this.viewModel.getTitle());
        this.setIconImage(getAppIcon());

        this.setMenuBar(buildMenuBar());

        this.toolBar = buildToolBar();
        this.setLayout(new BorderLayout());
        this.add(this.toolBar, BorderLayout.NORTH);

        leftPanel  = new JScrollPane();
        chunkTree = new ChunkTree(leftPanel, this.viewModel);

        rightPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        addTopTextArea();
        this.imageCanvas = new ImageCanvas(toolBar);
        this.textTree = new JTree();
        this.textTree.setVisible(false);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);

        this.add(splitPane);

        this.viewModel.addPropertyChangeListener(this::propertyChange);

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
        openItem.addActionListener(this::onOpenFile);

        fileMenu.add(openItem);
        menuBar.add(fileMenu);

        Menu viewMenu = new Menu("View");
        MenuItem showStatisticsItem = new MenuItem("Show statistics");
        showStatisticsItem.addActionListener((l) -> this.viewModel.showStatistics());
        viewMenu.add(showStatisticsItem);
        menuBar.add(viewMenu);

        return menuBar;
    }

    public void onOpenFile(ActionEvent actionEvent) {
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
        this.viewModel.loadFileAsync(file);
    }

    private void addTopTextArea() {
        topTextArea = new JTextArea(40, 60);
        topTextArea.setFont(MONOSPACED_FONT);
        topTextArea.setText(this.viewModel.getTopText());
        topTextArea.setEditable(false);

        JScrollPane scroll = new JScrollPane(topTextArea);
        this.rightPanel.setTopComponent(scroll);
    }

    private void propertyChange(PropertyChangeEvent evt) {

        JScrollPane bottomPanel = null;
        FieldName fieldName = FieldName.valueOf(evt.getPropertyName());
        switch (fieldName) {
            case TITLE:
                setTitle(this.viewModel.getTitle());
                break;
            case TREE_MODEL:
                chunkTree.setModel(viewModel.getTreeModel());
                break;
            case BUSY:
                setCursor(viewModel.isBusy() ?
                        Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) :
                        Cursor.getDefaultCursor());
                break;
            case TOP_TEXT:
                this.topTextArea.setText(this.viewModel.getTopText());
                break;
            case IMAGE:
                BufferedImage image = this.viewModel.getImage();
                if (image != null) {
                    imageCanvas.setImage(image);
                    imageCanvas.rePaint();
                    bottomPanel = new JScrollPane(imageCanvas);
                    this.rightPanel.setBottomComponent(bottomPanel);
                }
                break;
            case TEXT_TREE_MODEL:
                DefaultTreeModel treeModel = viewModel.getTextTreeModel();
                if (treeModel != null) {
                    textTree.setModel(treeModel);
                    textTree.setVisible(true);
                    bottomPanel = new JScrollPane(textTree);
                    this.rightPanel.setBottomComponent(bottomPanel);
                }
                break;
            default:
                break;
        }
    }

    private static Image getAppIcon() {
        URL appImageURL = MainFrame.class.getResource("/icons/djvu_app_icon_2_128.png");
        return Toolkit.getDefaultToolkit().getImage(appImageURL);
    }
}
