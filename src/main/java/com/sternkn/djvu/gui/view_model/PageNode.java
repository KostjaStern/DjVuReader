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

import com.sternkn.djvu.model.Page;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.util.Objects;

public class PageNode {
    public static final int WIDTH = 200;
    public static final int HEIGHT = 260;

    private final int index;
    private final Page page;
    private final ObjectProperty<Image> thumbnail;

    public PageNode(Page page, int index) {
        this.index = index;
        this.page = page;
        thumbnail = new SimpleObjectProperty<>(emptyImage());
    }

    public int getIndex() {
        return index;
    }

    public Page getPage() {
        return page;
    }

    public void setThumbnail(Image image) {
        this.thumbnail.set(image);
    }

    public ObjectProperty<Image> thumbnailProperty() {
        return thumbnail;
    }

    private Image emptyImage() {
        return new WritableImage(WIDTH, HEIGHT);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PageNode other)) {
            return false;
        }
        return Objects.equals(page, other.page);
    }

    @Override
    public int hashCode() {
        return Objects.hash(page);
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("PageNode{index: ");
        buffer.append(index);
        buffer.append(", id: ");
        buffer.append(page.getId());
        buffer.append(" , offset: ");
        buffer.append(page.getOffset());
        buffer.append("}");

        return buffer.toString();
    }
}
