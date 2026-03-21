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
package com.sternkn.djvu.gui;

import com.sternkn.djvu.gui.view.MainFrame;
import com.sternkn.djvu.gui.view_model.MainViewModel;
import com.sternkn.djvu.utils.LogUtils;
import javafx.application.Application;
import javafx.stage.Stage;


public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        var viewModel = new MainViewModel();
        var view = new MainFrame(viewModel, primaryStage);
        view.show();
    }

    public static void main(String[] args) {
        LogUtils.init();
        launch(args);
    }
}
