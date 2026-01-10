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

import com.sternkn.djvu.model.MenuNode;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.util.stream.Collectors;

public class MenuTreeItem extends TreeItem<MenuNode> {
    private boolean childrenLoaded = false;

    public MenuTreeItem(MenuNode node) {
        super(node);
    }

    @Override
    public ObservableList<TreeItem<MenuNode>> getChildren() {
        if (!childrenLoaded) {
            childrenLoaded = true;
            super.getChildren().setAll(
                    getValue().getChildren().stream()
                            .map(MenuTreeItem::new)
                            .collect(Collectors.toList())
            );
        }
        return super.getChildren();
    }

    @Override
    public boolean isLeaf() {
        return getValue().getChildren().isEmpty();
    }
}
