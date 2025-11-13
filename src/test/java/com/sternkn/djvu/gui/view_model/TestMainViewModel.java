package com.sternkn.djvu.gui.view_model;

import com.sternkn.djvu.file.DjVuFile;
import com.sternkn.djvu.file.chunks.Chunk;
import com.sternkn.djvu.file.chunks.ChunkId;
import com.sternkn.djvu.file.chunks.SecondaryChunkId;
import com.sternkn.djvu.file.chunks.TextZone;
import com.sternkn.djvu.model.ChunkInfo;
import com.sternkn.djvu.model.DjVuModel;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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

@ExtendWith(MockitoExtension.class)
public class TestMainViewModel {
    private static final double DELTA = 1e-9;

    private FileTaskFactory fileTaskFactory;
    private ChunkDecodingTaskFactory chunkDecodingTaskFactory;

    @Mock
    private DjVuModel djvuModel;

    private MainViewModel viewModel;

    @BeforeAll
    public static void beforeAll() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX Platform didn't start");
    }

    @BeforeEach
    public void setUp() {

    }

    @AfterEach
    public void tearDown() {

    }

    @Test
    public void testShowStatistics() {
        fileTaskFactory = mock(FileTaskFactory.class);
        chunkDecodingTaskFactory = mock(ChunkDecodingTaskFactory.class);
        viewModel = new MainViewModel(fileTaskFactory, chunkDecodingTaskFactory);
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
    public void testLoadFileAsyncSuccessCase() throws InterruptedException {

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

        fileTaskFactory = file -> new Task<>() {
            @Override
            protected DjVuFile call() {
                return djvuFile;
            }
        };
        chunkDecodingTaskFactory = mock(ChunkDecodingTaskFactory.class);
        viewModel = new MainViewModel(fileTaskFactory, chunkDecodingTaskFactory);
        viewModel.setDjvuModel(djvuModel);

        CountDownLatch finished = new CountDownLatch(2);
        viewModel.getProgress().addListener((obs, oldV, newV) -> {
            finished.countDown();
        });

        assertEquals(0.0, viewModel.getProgress().get(), DELTA);

        final String fileName = "sample.djvu";
        File file = new File(fileName);

        viewModel.loadFileAsync(file);

        assertEquals(ProgressBar.INDETERMINATE_PROGRESS, viewModel.getProgress().get(), DELTA);

        assertTrue(finished.await(3, TimeUnit.SECONDS), "loadFileAsync didn't finish in time");

        assertEquals(fileName, viewModel.getTitle().get());
        TreeItem<ChunkTreeNode> chunkRoot = viewModel.getChunkRootNode().get();
        assertEquals(new ChunkTreeNode(rootChunk), chunkRoot.getValue());

        assertTrue(viewModel.getErrorMessage().get().isEmpty(), "errorMessage must be empty on success");
        assertEquals(0.0, viewModel.getProgress().get(), DELTA);
    }

    @Test
    public void testLoadFileAsyncErrorCase() throws InterruptedException {
        final String errorMessage = "boom!";

        fileTaskFactory = file -> new Task<>() {
            @Override
            protected DjVuFile call() {
                throw new RuntimeException(errorMessage);
            }
        };
        chunkDecodingTaskFactory = mock(ChunkDecodingTaskFactory.class);
        viewModel = new MainViewModel(fileTaskFactory, chunkDecodingTaskFactory);
        viewModel.setDjvuModel(djvuModel);

        CountDownLatch finished = new CountDownLatch(1);
        viewModel.getErrorMessage().addListener((obs, ov, nv) -> {
            if (nv != null && !nv.isEmpty()) {
                finished.countDown();
            }
        });

        viewModel.loadFileAsync(new File("bad.djvu"));

        assertEquals(ProgressBar.INDETERMINATE_PROGRESS, viewModel.getProgress().get(), DELTA);

        assertTrue(finished.await(3, TimeUnit.SECONDS), "failure path didn't finish in time");

        assertEquals(errorMessage, viewModel.getErrorMessage().get());
        assertEquals(0.0, viewModel.getProgress().get(), DELTA);

        assertEquals(MainViewModel.APP_TITLE, viewModel.getTitle().get());
        assertNull(viewModel.getChunkRootNode().get());
    }

    @Test
    public void testShowChunkInfoWithTextZones() throws InterruptedException {
        ChunkInfo info = mock(ChunkInfo.class);

        TextZone root = mock(TextZone.class);
        TextZone child = mock(TextZone.class);
        when(root.getChildren()).thenReturn(List.of(child));
        when(child.getChildren()).thenReturn(List.of());

        when(info.getTextZones()).thenReturn(List.of(root));
        when(info.getTextData()).thenReturn("Some text data");

        fileTaskFactory = mock(FileTaskFactory.class);
        chunkDecodingTaskFactory = (model, chunkId) -> new Task<>() {
            @Override
            protected ChunkInfo call() {
                return info;
            }
        };
        viewModel = new MainViewModel(fileTaskFactory, chunkDecodingTaskFactory);
        viewModel.setDjvuModel(djvuModel);

        CountDownLatch finished = new CountDownLatch(2);
        viewModel.getProgress().addListener((obs, ov, nv) -> {
            finished.countDown();
        });

        viewModel.showChunkInfo(42L);

        assertEquals(ProgressBar.INDETERMINATE_PROGRESS, viewModel.getProgress().get(), DELTA);

        assertTrue(finished.await(3, TimeUnit.SECONDS), "showChunkInfo didn't finish in time");

        assertEquals("Some text data", viewModel.getTopText().get(), "topText must be set");
        assertTrue(viewModel.getShowTextTree().get(), "showTextTree must be true when zones exist");

        TreeItem<TextZoneNode> textRoot = viewModel.getTextRootNode().get();
        assertNotNull(textRoot, "textRootNode must be set");
        assertFalse(textRoot.getChildren().isEmpty(), "root must have children");

        assertNull(viewModel.getImage().get());

        assertTrue(viewModel.getErrorMessage().get().isEmpty(), "errorMessage must be empty on success");
        assertEquals(0.0, viewModel.getProgress().get(), DELTA);
    }

    @Test
    public void testShowChunkInfoWithError() throws InterruptedException {
        String errorMessage = "decode failed";
        fileTaskFactory = mock(FileTaskFactory.class);
        chunkDecodingTaskFactory = (model, chunkId) -> new Task<>() {
            @Override
            protected ChunkInfo call() {
                throw new RuntimeException(errorMessage);
            }
        };
        viewModel = new MainViewModel(fileTaskFactory, chunkDecodingTaskFactory);
        viewModel.setDjvuModel(djvuModel);

        CountDownLatch finished = new CountDownLatch(1);
        viewModel.getErrorMessage().addListener((obs, ov, nv) -> {
            finished.countDown();
        });

        viewModel.showChunkInfo(7L);

        assertEquals(ProgressBar.INDETERMINATE_PROGRESS, viewModel.getProgress().get(), DELTA);
        assertTrue(finished.await(3, TimeUnit.SECONDS), "failure path didn't finish in time");

        assertEquals(errorMessage, viewModel.getErrorMessage().get());
        assertEquals(0.0, viewModel.getProgress().get(), DELTA);

        assertNull(viewModel.getTextRootNode().get());
        assertNull(viewModel.getImage().get());
    }
}
