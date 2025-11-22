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

import com.sternkn.djvu.gui.view_model.MainViewModel;
import com.sternkn.djvu.gui.view_model.PageNode;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageCell extends ListCell<PageNode> {

    private static final Logger LOG = LoggerFactory.getLogger(PageCell.class);
    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    private final ImageView thumb = new ImageView();
    private final Label number = new Label();
    private final StackPane pane = new StackPane();

    private final MainViewModel viewModel;

    PageCell(MainViewModel viewModel) {
        this.viewModel = viewModel;

        thumb.setPreserveRatio(true);
        thumb.setFitWidth(90);
        thumb.setSmooth(true);

        final StackPane thumbCard = new StackPane();
        thumbCard.getChildren().add(thumb);
        thumbCard.setPadding(new Insets(6));
        thumbCard.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(6), Insets.EMPTY)));
        thumbCard.setEffect(new DropShadow(10, Color.rgb(0,0,0,0.25)));
        thumbCard.setMaxWidth(Region.USE_PREF_SIZE);

        number.getStyleClass().add("page-number");
        number.minHeightProperty().bind(thumbCard.heightProperty());

        final HBox content = new HBox(12);
        content.getChildren().addAll(thumbCard, number);
        content.setPadding(new Insets(10, 16, 10, 16));
        content.setFillHeight(false);

        pane.getStyleClass().add("page-cell");
        pane.getChildren().add(content);

        selectedProperty().addListener((obs, was, is) -> pane.pseudoClassStateChanged(SELECTED, is));

        setGraphic(pane);
        setText(null);

        setBackground(Background.EMPTY);
        setOnMouseClicked(this::onMouseClicked);
    }

    private void onMouseClicked(MouseEvent event) {
        LOG.debug("Mouse event button: {}", event.getButton());
        LOG.debug("Mouse event click count: {}", event.getClickCount());

        if (event.getButton() == MouseButton.PRIMARY) {
            PageNode page = getItem();
            LOG.debug("Page clicked: {}", page);

            viewModel.loadPageAsync(page);
        }
    }

    @Override
    protected void updateItem(PageNode page, boolean empty) {
        super.updateItem(page, empty);
        if (empty || page == null) {
            setGraphic(null);
        }
        else {
            thumb.setImage(page.getImage());
            number.setText(Integer.toString(page.getPage()));
            setGraphic(pane);
        }
    }
}
