package com.sternkn.djvu.gui.view;

public enum ToolBarButton {

    ZOOM_IN("zoom-in-32", "previous", "zoom in"),
    ZOOM_OUT("zoom-out-32", "up", "zoom out");


    ToolBarButton(String imageName, String actionCommand, String altText) {
        this.imageName = imageName;
        this.actionCommand = actionCommand;
        this.altText = altText;
    }

    private final String imageName;
    private final String actionCommand;
    private final String altText;

    public String getImageName() {
        return imageName;
    }

    public String getActionCommand() {
        return actionCommand;
    }

    public String getAltText() {
        return altText;
    }
}
