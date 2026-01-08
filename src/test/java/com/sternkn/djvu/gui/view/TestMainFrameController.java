/*
    Copyright (C) 2025 Kostya Stern

    This program is free software; you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation; either version 2 of the License, or (at your option)
    any later version.

    This program is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
    more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc., 51
    Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
*/
package com.sternkn.djvu.gui.view;

import com.sternkn.djvu.file.chunks.Chunk;
import com.sternkn.djvu.file.chunks.ChunkId;
import com.sternkn.djvu.file.chunks.SecondaryChunkId;
import com.sternkn.djvu.gui.view_model.ChunkTreeNode;
import com.sternkn.djvu.gui.view_model.MainViewModel;
import com.sternkn.djvu.gui.view_model.MenuNode;
import com.sternkn.djvu.gui.view_model.PageNode;
import com.sternkn.djvu.model.Page;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
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
import java.util.List;

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
        robot.interact(() -> viewModel.setDisableStatisticsMenu(false));

        robot.clickOn("View");
        robot.clickOn("#showStatisticsMenu");
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

    @Test
    public void testSelectPage(FxRobot robot) {
        List<Page> pages = List.of(
                new Page(1, 1L, "nb0001.djvu"),
                new Page(2, 25L, "nb0002.djvu"),
                new Page(3, 37L, "nb0003.djvu"));

        PageNode page2 = new PageNode(pages.get(1));
        doNothing().when(viewModel).loadPageAsync(page2);

        robot.interact(() -> viewModel.setPages(pages));

        robot.clickOn("Pages");
        robot.clickOn("2");

        verify(viewModel, times(1)).loadPageAsync(page2);
    }

    @Test
    public void testSelectPageByKeyPress(FxRobot robot) {
        List<Page> pages = List.of(
                new Page(1, 1L, "nb0001.djvu"),
                new Page(2, 25L, "nb0002.djvu"),
                new Page(3, 37L, "nb0003.djvu"));

        PageNode page2 = new PageNode(pages.get(1));
        PageNode page3 = new PageNode(pages.get(2));
        doNothing().when(viewModel).loadPageAsync(page2);
        doNothing().when(viewModel).loadPageAsync(page3);

        robot.interact(() -> viewModel.setPages(pages));

        robot.clickOn("Pages");
        robot.clickOn("2");
        robot.press(KeyCode.DOWN);

        verify(viewModel, times(1)).loadPageAsync(page2);
        verify(viewModel, times(1)).loadPageAsync(page3);
    }

    @Test
    public void testTableOfContextsDialog(FxRobot robot) {
        List<Page> pages = List.of(
                new Page(1, 1L, "nb0001.djvu"),
                new Page(2, 25L, "nb0002.djvu"),
                new Page(3, 37L, "nb0003.djvu"),
                new Page(4, 48L, "nb0004.djvu"),
                new Page(5, 51L, "nb0005.djvu"),
                new Page(6, 63L, "nb0006.djvu"));

        TreeItem<MenuNode> root = new TreeItem<>(new MenuNode("Root"));
        createMenuNode(root, "Content", "#nb0003.djvu");
        createMenuNode(root, "Preface", "#nb0005.djvu");

        PageNode page = new PageNode(pages.get(4));
        doNothing().when(viewModel).loadPageAsync(page);

        robot.interact(() -> viewModel.setPages(pages));
        robot.interact(() -> viewModel.setMenuRootNode(root));
        robot.interact(() -> viewModel.setDisableNavigationMenu(false));

        robot.clickOn("Pages");
        robot.clickOn("View");
        robot.clickOn("#navigationMenu");
        robot.clickOn("Preface");

        verify(viewModel, times(1)).loadPageAsync(page);
    }

    @Test
    public void testTableOfContextsDialogSubNode(FxRobot robot) {
        List<Page> pages = List.of(
                new Page(1, 1L, "nb0001.djvu"),
                new Page(2, 25L, "nb0002.djvu"),
                new Page(3, 37L, "nb0003.djvu"),
                new Page(4, 48L, "nb0004.djvu"),
                new Page(5, 51L, "nb0005.djvu"),
                new Page(6, 63L, "nb0006.djvu"),
                new Page(7, 78L, "nb0007.djvu"),
                new Page(8, 89L, "nb0008.djvu"),
                new Page(9, 95L, "nb0009.djvu"),
                new Page(10, 107L, "nb0010.djvu"),
                new Page(11, 119L, "nb0011.djvu"),
                new Page(12, 137L, "nb0012.djvu"));

        TreeItem<MenuNode> root = new TreeItem<>(new MenuNode("Root"));
        createMenuNode(root, "Content", "#3");
        TreeItem<MenuNode> node = createMenuNode(root, "Preface", "#5");
        createMenuNode(node, "Conventional designations", "#6");
        createMenuNode(node, "Using code examples", "#9");
        createMenuNode(node, "Acknowledgments", "#12");

        PageNode page = new PageNode(pages.get(11));

        doNothing().when(viewModel).loadPageAsync(page);

        robot.interact(() -> viewModel.setPages(pages));
        robot.interact(() -> viewModel.setMenuRootNode(root));
        robot.interact(() -> viewModel.setDisableNavigationMenu(false));

        robot.clickOn("Pages");
        robot.clickOn("View");
        robot.clickOn("#navigationMenu");

        robot.interact(() -> node.setExpanded(true));

        robot.clickOn("Acknowledgments");

        verify(viewModel, times(1)).loadPageAsync(page);
    }

    private TreeItem<MenuNode> createMenuNode(TreeItem<MenuNode> parent, String name, String url) {
        TreeItem<MenuNode> node = new TreeItem<>(new MenuNode(url, name));
        parent.getChildren().add(node);

        return node;
    }
}
