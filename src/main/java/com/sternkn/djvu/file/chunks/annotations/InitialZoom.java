package com.sternkn.djvu.file.chunks.annotations;

/**
 *  8.3.4.1.2 Initial Zoom
 *
 *  (zoom zoomvalue)
 */
public class InitialZoom extends Annotation {

    private final ZoomType zoomType;
    private final Integer zoomFactor;

    public InitialZoom(ZoomType zoomType, Integer zoomFactor) {
        super(AnnotationType.INITIAL_ZOOM);
        this.zoomType = zoomType;
        this.zoomFactor = zoomFactor;
    }

    public ZoomType getZoomType() {
        return zoomType;
    }

    public Integer getZoomFactor() {
        return zoomFactor;
    }
}
