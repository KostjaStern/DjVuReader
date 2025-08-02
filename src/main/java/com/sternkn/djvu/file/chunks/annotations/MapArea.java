package com.sternkn.djvu.file.chunks.annotations;


/**
 * 8.3.4.2 Maparea (overprinted annotations)
 *
 * (maparea url comment area ...)
 */
public class MapArea {

    /**
     *  Argument url takes either of these forms
     *      href
     *      (url href target)
     */
    private String url;
    private String target;

    /**
     * Argument comment is a string that might be displayed by the viewer when the user
     * moves the mouse over the maparea.
     */
    private String comment;



    public MapArea() {
    }
}
