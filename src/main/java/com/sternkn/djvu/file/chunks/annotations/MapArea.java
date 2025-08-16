package com.sternkn.djvu.file.chunks.annotations;


import java.util.Objects;

/**
 * 8.3.4.2 Maparea (overprinted annotations)
 *
 * (maparea url comment area ...)
 */
public class MapArea extends Annotation {

    /**
     *  Argument url takes either of these forms
     *      href
     *      (url href target)
     */
    private MapUrl url;

    /**
     * Argument comment is a string that might be displayed by the viewer when the user
     * moves the mouse over the maparea.
     */
    private String comment;

    private Area area;

    public MapArea(MapUrl url, String comment, Area area) {
        super(AnnotationType.MAP_AREA);
        this.url = url;
        this.comment = comment;
        this.area = area;
    }

    public MapUrl getUrl() {
        return url;
    }

    public String getComment() {
        return comment;
    }

    public Area getArea() {
        return area;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MapArea other)) {
            return false;
        }

        return type == other.getType()
                && Objects.equals(this.url, other.url)
                && Objects.equals(this.comment, other.comment)
                && Objects.equals(this.area, other.area);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, url, comment, area);
    }
}
