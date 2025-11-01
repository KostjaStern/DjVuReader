package com.sternkn.djvu.gui_java_fx;

import com.sternkn.djvu.gui_java_fx.view.MainFrame;
import com.sternkn.djvu.gui_java_fx.view_model.MainViewModel;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        var viewModel = new MainViewModel();
        var view = new MainFrame(viewModel, primaryStage);
        view.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
