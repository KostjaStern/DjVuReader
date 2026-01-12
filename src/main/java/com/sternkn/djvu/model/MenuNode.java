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
package com.sternkn.djvu.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MenuNode {

    private final String title;
    private final Integer pageNumber;
    private final List<MenuNode> children;

    public MenuNode(String title, Integer pageNumber) {
        this(title, pageNumber, new ArrayList<>());
    }

    public MenuNode(String title, Integer pageNumber, List<MenuNode> children) {
        this.title = title;
        this.pageNumber = pageNumber;
        this.children = children;
    }

    public String getTitle() {
        return title;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public List<MenuNode> getChildren() {
        return children;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MenuNode other)) {
            return false;
        }
        return Objects.equals(title, other.title)
                && Objects.equals(pageNumber, other.pageNumber)
                && Objects.equals(children, other.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, pageNumber, children);
    }

    @Override
    public String toString() {
        return title;
    }
}
