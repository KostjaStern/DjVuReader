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
