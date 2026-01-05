package com.sternkn.djvu.gui.view;

import com.sternkn.djvu.gui.view_model.MainViewModel;
import com.sternkn.djvu.gui.view_model.MenuNode;
import com.sternkn.djvu.gui.view_model.PageNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

public class TableOfContentsDialogController {

    private static final Logger LOG = LoggerFactory.getLogger(TableOfContentsDialogController.class);

    private final Stage stage;
    private final MainViewModel viewModel;

    @FXML
    private TreeView<MenuNode> menuTree;

    private final ListView<PageNode> pageList;

    public TableOfContentsDialogController(MainViewModel viewModel, ListView<PageNode> pageList, Stage stage) {
        this.viewModel = viewModel;
        this.pageList = pageList;
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        LOG.info("Initializing TableOfContentsDialogController ...");

        menuTree.rootProperty().bind(viewModel.getMenuRootNode());
        menuTree.setCellFactory(tv -> new MenuTreeCell(this));
    }

    public ListView<PageNode> getPageList() {
        return pageList;
    }

    public void scrollToPage(MenuNode menuNode) {
        if (menuNode == null) {
            return;
        }

        Optional<PageNode> pageNode = pageList.itemsProperty().getValue().stream()
            .filter(p -> Objects.equals(p.getPage().getId(), menuNode.getPageId())
                                   || Objects.equals(p.getPage().getIndex(), menuNode.getPage()))
            .findFirst();

        if (pageNode.isEmpty()) {
            return;
        }

        PageNode node = pageNode.get();

        Platform.runLater(() -> {
            pageList.getSelectionModel().select(node);
            pageList.scrollTo(node);
        });
    }
}
