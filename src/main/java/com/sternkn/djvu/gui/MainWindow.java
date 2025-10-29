package com.sternkn.djvu.gui;

import com.sternkn.djvu.gui.view.MainFrame;
import com.sternkn.djvu.gui.view_model.MainViewModel;

import javax.swing.SwingUtilities;

public class MainWindow {

    static {
        System.setProperty("apple.awt.application.name", MainViewModel.APP_TITLE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            var viewModel = new MainViewModel();
            var view = new MainFrame(viewModel);
            view.setVisible(true);
        });
    }
}
