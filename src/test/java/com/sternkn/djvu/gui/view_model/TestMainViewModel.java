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
package com.sternkn.djvu.gui.view_model;

import com.sternkn.djvu.file.DjVuFile;
import com.sternkn.djvu.file.chunks.Chunk;
import com.sternkn.djvu.file.chunks.ChunkId;
import com.sternkn.djvu.file.chunks.ComponentInfo;
import com.sternkn.djvu.file.chunks.ComponentType;
import com.sternkn.djvu.file.chunks.DirectoryChunk;
import com.sternkn.djvu.file.chunks.SecondaryChunkId;
import com.sternkn.djvu.file.chunks.TextZone;
import com.sternkn.djvu.model.ChunkInfo;
import com.sternkn.djvu.model.DjVuModel;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
public class TestMainViewModel {
    private static final double DELTA = 1e-9;

    private FileTaskFactory fileTaskFactory;
    private ChunkDecodingTaskFactory chunkDecodingTaskFactory;
    private PageLoadingTaskFactory pageLoadingTaskFactory;
    private ThumbnailLoadingTaskFactory thumbnailLoadingTaskFactory;

    @Mock
    private DjVuModel djvuModel;

    private MainViewModel viewModel;

    @Test
    public void testShowStatistics() {
        fileTaskFactory = mock(FileTaskFactory.class);
        chunkDecodingTaskFactory = mock(ChunkDecodingTaskFactory.class);
        pageLoadingTaskFactory = mock(PageLoadingTaskFactory.class);
        thumbnailLoadingTaskFactory = mock(ThumbnailLoadingTaskFactory.class);
        viewModel = new MainViewModel(
            fileTaskFactory, chunkDecodingTaskFactory, pageLoadingTaskFactory, thumbnailLoadingTaskFactory);
        viewModel.setDjvuModel(djvuModel);

        final String statistics = "Some statistics ... ";
        when(djvuModel.getChunkStatistics()).thenReturn(statistics);

        AtomicReference<String> oldText = new AtomicReference<>();
        AtomicReference<String> newText = new AtomicReference<>();

        viewModel.getTopText().addListener((obs, oldVal, newVal) -> {
            oldText.set(oldVal);
            newText.set(newVal);
        });

        viewModel.showStatistics();

        assertEquals("", oldText.get());
        assertEquals(statistics, newText.get());

        verify(djvuModel, times(1)).getChunkStatistics();
    }

    @Test
    public void testLoadFileAsyncSuccessCase() {
        DjVuFile djvuFile = mock(DjVuFile.class);
        Chunk rootChunk = Chunk.builder()
            .withChunkId(ChunkId.FORM)
            .withSecondaryChunkId(SecondaryChunkId.DJVM)
            .withId(0L).withSize(1L).build();

        Chunk childChunk = Chunk.builder()
                .withChunkId(ChunkId.DIRM)
                .withParent(rootChunk)
                .withId(1L).withSize(1L).build();

        when(djvuFile.getChunks()).thenReturn(List.of(rootChunk, childChunk));

        DirectoryChunk directoryChunk = mock(DirectoryChunk.class);
        ComponentInfo component1 = mock(ComponentInfo.class);
        when(component1.getType()).thenReturn(ComponentType.PAGE);
        when(component1.getOffset()).thenReturn(23L);

        ComponentInfo component2 = mock(ComponentInfo.class);
        when(component2.getType()).thenReturn(ComponentType.PAGE);
        when(component2.getOffset()).thenReturn(1357L);

        when(directoryChunk.getComponents()).thenReturn(List.of(component1, component2));
        when(djvuFile.getDirectoryChunk()).thenReturn(directoryChunk);

        chunkDecodingTaskFactory = mock(ChunkDecodingTaskFactory.class);
        pageLoadingTaskFactory = mock(PageLoadingTaskFactory.class);

        fileTaskFactory = file -> new Task<>() {
            @Override
            public DjVuFile call() {
                return djvuFile;
            }
        };
        thumbnailLoadingTaskFactory = (MainViewModel mod, DjVuModel djvuMod) -> new Task<>() {
            @Override
            public Void call() {
                mod.setProgressMessage("");
                mod.setProgress(0);
                return null;
            }
        };

        viewModel = new MainViewModel(fileTaskFactory, chunkDecodingTaskFactory,
                pageLoadingTaskFactory, thumbnailLoadingTaskFactory);
        viewModel.setDjvuModel(djvuModel);

        assertEquals(0.0, viewModel.getProgress().get(), DELTA);

        final String fileName = "sample.djvu";
        File file = new File(fileName);

        viewModel.loadFileAsync(file);

        waitForFxEvents();

        assertEquals(fileName, viewModel.getTitle().get());
        TreeItem<ChunkTreeNode> chunkRoot = viewModel.getChunkRootNode().get();
        assertEquals(new ChunkTreeNode(rootChunk), chunkRoot.getValue());
        assertEquals(List.of(new PageNode(1, 23L), new PageNode(2, 1357L)),
                viewModel.getPages().stream().toList());

        assertTrue(viewModel.getProgressMessage().get().isEmpty(), "errorMessage must be empty on success");
        assertEquals(0.0, viewModel.getProgress().get(), DELTA);
    }

    @Test
    public void testLoadFileAsyncErrorCase() {
        final String errorMessage = "boom!";

        fileTaskFactory = file -> new Task<>() {
            @Override
            public DjVuFile call() {
                throw new RuntimeException(errorMessage);
            }
        };
        chunkDecodingTaskFactory = mock(ChunkDecodingTaskFactory.class);
        pageLoadingTaskFactory = mock(PageLoadingTaskFactory.class);
        thumbnailLoadingTaskFactory = mock(ThumbnailLoadingTaskFactory.class);
        viewModel = new MainViewModel(
            fileTaskFactory, chunkDecodingTaskFactory, pageLoadingTaskFactory, thumbnailLoadingTaskFactory);
        viewModel.setDjvuModel(djvuModel);

        viewModel.loadFileAsync(new File("bad.djvu"));

        assertEquals(ProgressBar.INDETERMINATE_PROGRESS, viewModel.getProgress().get(), DELTA);

        waitForFxEvents();

        assertEquals(errorMessage, viewModel.getProgressMessage().get());
        assertEquals(MainViewModel.APP_TITLE, viewModel.getTitle().get());
        assertNull(viewModel.getChunkRootNode().get());
    }

    @Test
    public void testShowChunkInfoWithTextZones() {
        ChunkInfo info = mock(ChunkInfo.class);

        TextZone root = mock(TextZone.class);
        TextZone child = mock(TextZone.class);
        when(root.getChildren()).thenReturn(List.of(child));
        when(child.getChildren()).thenReturn(List.of());

        when(info.getTextZones()).thenReturn(List.of(root));
        when(info.getTextData()).thenReturn("Some text data");

        fileTaskFactory = mock(FileTaskFactory.class);
        pageLoadingTaskFactory = mock(PageLoadingTaskFactory.class);
        chunkDecodingTaskFactory = (model, chunkId) -> new Task<>() {
            @Override
            protected ChunkInfo call() {
                return info;
            }
        };
        thumbnailLoadingTaskFactory = mock(ThumbnailLoadingTaskFactory.class);
        viewModel = new MainViewModel(
            fileTaskFactory, chunkDecodingTaskFactory, pageLoadingTaskFactory, thumbnailLoadingTaskFactory);
        viewModel.setDjvuModel(djvuModel);

        viewModel.showChunkInfo(42L);

        assertEquals(ProgressBar.INDETERMINATE_PROGRESS, viewModel.getProgress().get(), DELTA);

        waitForFxEvents();

        assertEquals("Some text data", viewModel.getTopText().get(), "topText must be set");
        assertTrue(viewModel.getShowTextTree().get(), "showTextTree must be true when zones exist");

        TreeItem<TextZoneNode> textRoot = viewModel.getTextRootNode().get();
        assertNotNull(textRoot, "textRootNode must be set");
        assertFalse(textRoot.getChildren().isEmpty(), "root must have children");

        assertNull(viewModel.getImage().get());

        assertTrue(viewModel.getProgressMessage().get().isEmpty(), "errorMessage must be empty on success");
        assertEquals(0.0, viewModel.getProgress().get(), DELTA);
    }

    @Test
    public void testShowChunkInfoWithError() {
        String errorMessage = "decode failed";
        fileTaskFactory = mock(FileTaskFactory.class);
        pageLoadingTaskFactory = mock(PageLoadingTaskFactory.class);
        chunkDecodingTaskFactory = (model, chunkId) -> new Task<>() {
            @Override
            protected ChunkInfo call() {
                throw new RuntimeException(errorMessage);
            }
        };
        thumbnailLoadingTaskFactory = mock(ThumbnailLoadingTaskFactory.class);
        viewModel = new MainViewModel(
            fileTaskFactory, chunkDecodingTaskFactory, pageLoadingTaskFactory, thumbnailLoadingTaskFactory);
        viewModel.setDjvuModel(djvuModel);

        viewModel.showChunkInfo(7L);

        assertEquals(ProgressBar.INDETERMINATE_PROGRESS, viewModel.getProgress().get(), DELTA);

        waitForFxEvents();

        assertEquals(errorMessage, viewModel.getProgressMessage().get());
        assertEquals(0.0, viewModel.getProgress().get(), DELTA);
        assertNull(viewModel.getTextRootNode().get());
        assertNull(viewModel.getImage().get());
    }
}
