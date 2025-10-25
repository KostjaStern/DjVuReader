package com.sternkn.djvu.gui.view;

import com.sternkn.djvu.gui.GuiTestSupport;
import com.sternkn.djvu.gui.view_model.ChunkDecodingWorkerFactory;
import com.sternkn.djvu.gui.view_model.FileWorkerFactory;
import com.sternkn.djvu.gui.view_model.MainViewModel;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.extension.GUITestExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith({GUITestExtension.class, MockitoExtension.class})
public class TestMainFrame extends GuiTestSupport {

    @Mock
    private FileWorkerFactory fileWorkerFactory;

    @Mock
    private ChunkDecodingWorkerFactory chunkDecodingWorkerFactory;

    @TempDir
    Path tempDir;

    private MainViewModel viewModel;
    private FrameFixture window;

    @BeforeAll
    public static void setUpOnce() {
        // Disabling the macOS OSD menu
        // System.setProperty("apple.laf.useScreenMenuBar", "false");
        FailOnThreadViolationRepaintManager.install();
    }

    @BeforeEach
    public void setUp() {
        viewModel = new MainViewModel(fileWorkerFactory, chunkDecodingWorkerFactory);
        MainFrame frame = GuiActionRunner.execute(() -> new MainFrame(viewModel));

        assertNotNull(frame);
        window = new FrameFixture(frame);
        window.show();
    }

    @AfterEach
    public void tearDown() {
        window.cleanUp();
    }

    @Test
    public void testMainFrameTitle() {
        window.requireTitle(MainViewModel.APP_TITLE);
    }

    /*
    @Test
    public void testMainFrameOpenFileDialog() throws IOException {
        String fileName = "sample.djvu";
        Path path = tempDir.resolve(fileName);
        Files.writeString(path, "dummy");
        File file = tempDir.resolve(fileName).toFile();

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        DefaultTreeModel model = new DefaultTreeModel(root);

        SwingWorker<DjVuFile, Void> worker = createFileWorker(viewModel, file, model);

        when(fileWorkerFactory.create(viewModel, file)).thenReturn(worker);

        JMenuItemFixture fileMenuItem = window.menuItem(ControlName.FILE_MENU.name());
        fileMenuItem.focus().click();

        window.menuItem(ControlName.FILE_OPEN_MENU.name()).click();
        JFileChooserFixture chooser = window.fileChooser(ControlName.OPEN_DJVU_FILE_DIALOG.name());
        chooser.selectFile(file);
        chooser.approve();

        window.requireTitle(fileName);
    }

     */
}
