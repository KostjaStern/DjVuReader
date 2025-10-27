package com.sternkn.djvu.gui.view_model;

import com.sternkn.djvu.file.DjVuFile;
import com.sternkn.djvu.gui.GuiTestSupport;
import com.sternkn.djvu.model.DjVuModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestMainViewModel extends GuiTestSupport {

    @Mock
    private FileWorkerFactory fileWorkerFactory;

    @Mock
    private ChunkDecodingWorkerFactory chunkDecodingWorkerFactory;
    private MainViewModel viewModel;

    @Mock
    private DjVuModel djvuModel;
    private PropertyChangeListener listener;

    @BeforeEach
    public void setUp() {
        viewModel = new MainViewModel(fileWorkerFactory, chunkDecodingWorkerFactory);
        viewModel.setDjvuModel(djvuModel);
    }

    @AfterEach
    public void tearDown() {
        if (listener != null) {
            viewModel.removePropertyChangeListener(listener);
        }
    }

    @Test
    public void testShowStatistics() {
        final String statistics = "Some statistics ... ";
        when(djvuModel.getChunkStatistics()).thenReturn(statistics);

        final AtomicInteger events = new AtomicInteger(0);
        final AtomicReference<PropertyChangeEvent> lastEvent = new AtomicReference<>();

        listener = event -> {
            if (event.getPropertyName().equals(FieldName.TOP_TEXT.name())) {
                events.incrementAndGet();
                lastEvent.set(event);
            }
        };

        viewModel.addPropertyChangeListener(listener);

        viewModel.showStatistics();

        assertEquals(statistics, viewModel.getTopText());
        assertEquals(1, events.get());

        PropertyChangeEvent event = lastEvent.get();
        assertEquals(FieldName.TOP_TEXT.name(), event.getPropertyName());
        assertEquals("", event.getOldValue());
        assertEquals(statistics, event.getNewValue());

        verify(djvuModel, times(1)).getChunkStatistics();
    }

    @Test
    public void testLoadFileAsync() throws InterruptedException {

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        DefaultTreeModel model = new DefaultTreeModel(root);
        String fileName = "dummy.djvu";

        File file = new File(fileName);
        SwingWorker<DjVuFile, Void> worker = createFileWorker(viewModel, file, model);

        when(fileWorkerFactory.create(viewModel, file)).thenReturn(worker);

        var latch = new CountDownLatch(2);
        viewModel.addPropertyChangeListener(e -> {
            if (FieldName.TREE_MODEL.name().equals(e.getPropertyName())
                    || FieldName.TITLE.name().equals(e.getPropertyName())) {
                latch.countDown();
            }
        });

        viewModel.loadFileAsync(file);

        assertTrue(latch.await(1, TimeUnit.SECONDS), "We did not wait for the events");

        assertEquals(fileName, viewModel.getTitle());
        assertEquals(model, viewModel.getTreeModel());
        assertFalse(viewModel.isBusy());
    }
}
