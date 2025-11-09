package com.sternkn.djvu.gui_java_fx.view;

import com.sternkn.djvu.gui_java_fx.view_model.MainViewModel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
        protected FileChooser createFileChooser() {
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
        robot.clickOn("#zoomInButton");

        verifyThat("#zoomValue", hasText("1.01"));
    }

    @Test
    public void testZoomOut(FxRobot robot) {
        robot.clickOn("#zoomOutButton");

        verifyThat("#zoomValue", hasText("0.99"));
    }

    @Test
    public void testOnOpenFile(FxRobot robot) {
        String fileName = "sample.djvu";
        File fakeFile = tempDir.resolve(fileName).toFile();
        when(this.controller.createFileChooser().showOpenDialog(any())).thenReturn(fakeFile);

        doNothing().when(viewModel).loadFileAsync(any(File.class));

        robot.clickOn("#fileMenu");
        robot.clickOn("#openMenuItem");

        verify(viewModel, times(1))
            .loadFileAsync(argThat(file -> file != null && file.getName().equals(fileName)));
    }
}
