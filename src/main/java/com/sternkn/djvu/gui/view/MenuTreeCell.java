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
package com.sternkn.djvu.gui.view;

import com.sternkn.djvu.model.MenuNode;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class MenuTreeCell extends TreeCell<MenuNode> {

    private final Label titleLabel;
    private final Label pageLabel;
    private final HBox box;

    public MenuTreeCell() {
        this.titleLabel = new Label();
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        this.pageLabel = new Label();

        Region spacer = new Region();
        this.box = new HBox(8, titleLabel, spacer, pageLabel);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        pageLabel.setAlignment(Pos.CENTER_RIGHT);
        pageLabel.setMinWidth(50);
        pageLabel.setPrefWidth(50);
        pageLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
    }

    @Override
    public void updateItem(MenuNode item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        }
        else {
            titleLabel.setText(item.getTitle());

            String number = item.getPageNumber() == null ? "" : item.getPageNumber().toString();
            pageLabel.setText(number);
            setText(null);
            setGraphic(box);
        }
    }
}
