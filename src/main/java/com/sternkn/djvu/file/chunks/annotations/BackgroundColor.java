package com.sternkn.djvu.file.chunks.annotations;

import com.sternkn.djvu.file.chunks.Color;

/**
 *   8.3.4.1.1 Background Color
 *
 *   (background color)
 */
public class BackgroundColor extends Annotation {

    private final Color color;

    public BackgroundColor(Color color) {
        super(AnnotationType.BACKGROUND_COLOR);
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        return String.format("{color: %s}", color);
    }
}
