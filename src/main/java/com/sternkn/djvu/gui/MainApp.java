package com.sternkn.djvu.gui;

import com.sternkn.djvu.gui.view.MainFrame;
import com.sternkn.djvu.gui.view_model.MainViewModel;
import javafx.application.Application;
import javafx.stage.Stage;

import java.awt.Taskbar;
import java.awt.Toolkit;
import java.awt.Image;
import java.net.URL;

public class MainApp extends Application {

    static {
        if (Taskbar.isTaskbarSupported()) {
            Taskbar tb = Taskbar.getTaskbar();
            if (tb.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                tb.setIconImage(getAppIcon());
            }
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        var viewModel = new MainViewModel();
        var view = new MainFrame(viewModel, primaryStage);
        view.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static Image getAppIcon() {
        URL appImageURL = MainApp.class.getResource("/icons/djvu_app_icon_2_128.png");
        return Toolkit.getDefaultToolkit().getImage(appImageURL);
    }
}
