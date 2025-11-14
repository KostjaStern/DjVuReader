package com.sternkn.djvu.gui.view;

import com.sternkn.djvu.file.chunks.Chunk;
import com.sternkn.djvu.file.chunks.ChunkId;
import com.sternkn.djvu.file.chunks.SecondaryChunkId;
import com.sternkn.djvu.gui.view_model.ChunkTreeNode;
import com.sternkn.djvu.gui.view_model.MainViewModel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TextInputControlMatchers.hasText;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.argThat;

@ExtendWith(ApplicationExtension.class)
public class TestMainFrameController {

    private MainViewModel viewModel;

    @TempDir
    private Path tempDir;

    private MainFrameController controller;

    static class TestableMainFrameController extends MainFrameController {

        private final FileChooser chooser;

        TestableMainFrameController(MainViewModel viewModel, Stage stage) {
            super(viewModel, stage);
            chooser = mock(FileChooser.class);
        }

        @Override
        FileChooser openFileDialog() {
            return chooser;
        }

        @Override
        FileChooser saveChunkDataDialog() {
            return chooser;
        }
    }

    @Start
    public void start(Stage stage) throws IOException {
        viewModel = spy(new MainViewModel());
        controller = new TestableMainFrameController(viewModel, stage);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainFrame.fxml"));
        loader.setController(controller);

        Parent root = loader.load();
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testZoomIn(FxRobot robot) {
        doNothing().when(viewModel).zoomIn();

        robot.clickOn("#zoomInButton");

        verify(viewModel, times(1)).zoomIn();
    }

    @Test
    public void testZoomOut(FxRobot robot) {
        doNothing().when(viewModel).zoomOut();

        robot.clickOn("#zoomOutButton");

        verify(viewModel, times(1)).zoomOut();
    }

    @Test
    public void testOpenFileMenu(FxRobot robot) {
        String fileName = "sample.djvu";
        File fakeFile = tempDir.resolve(fileName).toFile();
        when(this.controller.openFileDialog().showOpenDialog(any())).thenReturn(fakeFile);

        doNothing().when(viewModel).loadFileAsync(any(File.class));

        robot.clickOn("#fileMenu");
        robot.clickOn("#openMenuItem");

        verify(viewModel, times(1))
            .loadFileAsync(argThat(file -> file != null && file.getName().equals(fileName)));
    }

    @Test
    public void testShowStatistics(FxRobot robot) {
        String statistics =
            """
                Composite chunks
            ---------------------------------
             FORM:DJVU      : 534
             FORM:DJVI      : 54
             FORM:DJVM      : 1
            
            
                Data chunks
            ---------------------------------
             DIRM           : 1
             Sjbz           : 533
             BG44           : 4
             INCL           : 532
             CIDa           : 534
             INFO           : 534
             ANTz           : 187
             TXTz           : 528
             Djbz           : 54
            """;

        doNothing().when(viewModel).showStatistics();

        robot.clickOn("View");
        robot.clickOn("#showStatistics");
        robot.interact(() -> viewModel.setTopText(statistics));

        verify(viewModel, times(1)).showStatistics();
        verifyThat("#topTextArea", hasText(statistics));
    }

    @Test
    public void testChunkTreeContextMenu(FxRobot robot) {
        Chunk rootChunk = Chunk.builder()
                .withChunkId(ChunkId.FORM)
                .withSecondaryChunkId(SecondaryChunkId.DJVM)
                .withId(0L).withSize(1L).build();

        Chunk childChunk = Chunk.builder()
                .withChunkId(ChunkId.DIRM)
                .withParent(rootChunk)
                .withId(1L).withSize(1L).build();

        ChunkTreeNode rootNode = new ChunkTreeNode(rootChunk);
        ChunkTreeNode childNode = new ChunkTreeNode(childChunk);

        TreeItem<ChunkTreeNode> root = new TreeItem<>(rootNode);
        TreeItem<ChunkTreeNode> child = new TreeItem<>(childNode);
        root.getChildren().add(child);

        String fileName = String.format("%s_%s.data", childNode.getChunkName(), childNode.getChunkId());
        File fakeFile = tempDir.resolve(fileName).toFile();

        when(controller.saveChunkDataDialog().showSaveDialog(any())).thenReturn(fakeFile);
        doNothing().when(viewModel).saveChunkData(any(File.class), anyLong());

        robot.interact(() -> viewModel.getChunkRootNode().set(root));

        robot.doubleClickOn(rootNode.toString());
        robot.rightClickOn(childNode.toString());
        robot.clickOn(ChunkTreeCell.SAVE_CHUNK_DATA);

        verify(controller.saveChunkDataDialog(), times(1)).setInitialFileName(fileName);
        verify(viewModel, times(1)).saveChunkData(fakeFile,  childNode.getChunkId());
    }
}
