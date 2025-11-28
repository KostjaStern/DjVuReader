package com.sternkn.djvu.model;

import javafx.scene.image.Image;

public class Page {
    private Image image;

    public Page(Image image) {
        this.image = image;
    }

    public Image getImage() {
        return image;
    }
}
