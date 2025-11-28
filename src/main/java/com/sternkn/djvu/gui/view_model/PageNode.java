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
package com.sternkn.djvu.gui.view_model;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.util.Objects;

public class PageNode {
    private int page;
    private Long offset;
    private Image image;
    private boolean loaded;

    public PageNode(int page, Long offset) {
        this.page = page;
        this.offset = offset;
        image = emptyImage();
        loaded = false;
    }

    public int getPage() {
        return page;
    }

    public Long getOffset() {
        return offset;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public Image getImage() {
        return image;
    }
//    public void setImage(Image image) {
//        this.loaded = true;
//        this.image = image;
//    }

    private Image emptyImage() {
        return new WritableImage(200, 260);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PageNode other)) {
            return false;
        }
        return Objects.equals(page, other.page)
            && Objects.equals(offset, other.offset)
            && Objects.equals(loaded, other.loaded);
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, offset, loaded);
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("PageNode{ ");
        buffer.append(page);
        buffer.append(" , ");
        buffer.append(offset);
        buffer.append(" , ");
        buffer.append(loaded);
        buffer.append("}");

        return buffer.toString();
    }
}
