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

    @Override
    public String toString() {
        return String.format("{url: %s, comment: %s, area: %s}", url,  comment, area);
    }
}
