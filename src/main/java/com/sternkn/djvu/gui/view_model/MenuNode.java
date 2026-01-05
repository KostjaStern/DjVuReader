package com.sternkn.djvu.gui.view_model;

import com.sternkn.djvu.file.chunks.Bookmark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MenuNode {
    private static final Logger LOG = LoggerFactory.getLogger(MenuNode.class);

    private final String url;
    private final String nodeName;
    private final String pageId;
    private Integer page;

    public MenuNode(Bookmark bookmark) {
        this.nodeName = bookmark.sDesc();
        this.url = bookmark.sURL();

        if (url != null && url.length() > 1) {
            pageId = url.substring(1);

            try {
                page = Integer.parseInt(pageId);
            }
            catch (NumberFormatException e) {
                LOG.warn("Invalid page number: {} for bookmark: {}", pageId, bookmark);
                page = null;
            }
        }
        else {
            page = null;
            pageId = null;
        }
    }

    public MenuNode(String nodeName) {
        this.nodeName = nodeName;
        this.page = null;
        this.pageId = null;
        this.url = null;
    }

    public String getUrl() {
        return url;
    }

    public String getNodeName() {
        return nodeName;
    }

    public Integer getPage() {
        return page;
    }

    public String getPageId() {
        return pageId;
    }

    @Override
    public String toString() {
        return nodeName;
    }
}
