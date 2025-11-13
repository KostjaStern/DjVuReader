package com.sternkn.djvu.gui.view;

import com.sternkn.djvu.file.DjVuFileException;
import com.sternkn.djvu.gui.view_model.MainViewModel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFrame {
    private final static double SCREEN_WIDTH = 800;
    private final static double SCREEN_HEIGHT = 600;

    private final MainViewModel viewModel;
    private final Stage stage;

    public MainFrame(MainViewModel model, Stage stage) {
        this.viewModel = model;
        this.stage = stage;

        Parent root = loadRootNode();

        Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        stage.titleProperty().bind(viewModel.getTitle());
        stage.setScene(scene);
    }

    private Parent loadRootNode() {
        MainFrameController controller = new MainFrameController(viewModel, stage);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainFrame.fxml"));
        loader.setController(controller);

        try {
            return loader.load();
        }
        catch (IOException exception) {
            throw new DjVuFileException("Unable to load FXML file for main window", exception);
        }
    }

    public void show() {
        stage.show();
    }
}
