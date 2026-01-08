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
        this(bookmark.sURL(), bookmark.sDesc());
    }

    public MenuNode(String url, String desc) {
        this.nodeName = desc;
        this.url = url;

        if (url != null && url.length() > 1) {
            pageId = url.substring(1);

            try {
                page = Integer.parseInt(pageId);
            }
            catch (NumberFormatException e) {
                LOG.warn("Invalid page number: {}", pageId);
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
