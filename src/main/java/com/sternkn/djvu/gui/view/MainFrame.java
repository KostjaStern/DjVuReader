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

import com.sternkn.djvu.file.DjVuFileException;
import com.sternkn.djvu.gui.view_model.MainViewModel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

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
        scene.getStylesheets().add(getResource("/css/pages.css").toExternalForm());
        stage.titleProperty().bind(viewModel.getTitle());
        stage.setScene(scene);
    }

    private Parent loadRootNode() {
        MainFrameController controller = new MainFrameController(viewModel, stage);
        FXMLLoader loader = new FXMLLoader(getResource("/views/MainFrame.fxml"));
        loader.setController(controller);

        try {
            return loader.load();
        }
        catch (IOException exception) {
            throw new DjVuFileException("Unable to load FXML file for main window", exception);
        }
    }

    private URL getResource(String name) {
        return getClass().getResource(name);
    }

    public void show() {
        stage.show();
    }
}
