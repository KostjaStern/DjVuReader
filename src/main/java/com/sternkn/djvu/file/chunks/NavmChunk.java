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
package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.coders.BSByteInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static com.sternkn.djvu.utils.utils.InputStreamUtils.read16;
import static com.sternkn.djvu.utils.utils.InputStreamUtils.read24;
import static com.sternkn.djvu.utils.utils.InputStreamUtils.readString;
import static com.sternkn.djvu.utils.utils.StringUtils.NL;
import static com.sternkn.djvu.utils.utils.StringUtils.padRight;

public class NavmChunk extends Chunk {
    private static final Logger LOG = LoggerFactory.getLogger(NavmChunk.class);

    private final List<Bookmark> bookmarks;

    public NavmChunk(Chunk chunk) {
        super(chunk);
        final ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
        final BSByteInputStream bzzData = new BSByteInputStream(byteStream);

        final int countBookmarks = read16(bzzData);
        LOG.debug("countBookmarks = {}", countBookmarks);

        bookmarks = new ArrayList<>();
        for (int i = 0; i < countBookmarks; i++) {
            int nChildren = bzzData.read();
            int nDesc = read24(bzzData);
            String sDesc = readString(bzzData, nDesc);
            int nURL = read24(bzzData);
            String sURL = readString(bzzData, nURL);

            bookmarks.add(new Bookmark(nChildren,  nDesc, sDesc, nURL, sURL));
        }
    }

    @Override
    public String getDataAsText() {
        String parentData = super.getDataAsText();
        StringBuilder buffer = new StringBuilder();
        buffer.append(parentData);
        buffer.append(" Count bookmarks: ").append(bookmarks.size()).append(NL).append(NL);

        int maxDescLength = bookmarks.stream()
                .map(b -> b.sDesc().length())
                .max(Integer::compareTo).orElse(0) + 10;
        int maxUrlLength = bookmarks.stream()
                .map(b -> b.sURL().length())
                .max(Integer::compareTo).orElse(0) + 10;
        buffer.append(padRight(" nChildren", 15))
              .append(padRight(" nDesc", 15))
              .append(padRight(" sDesc", maxDescLength))
              .append(padRight(" nURL", 15))
              .append(padRight(" sURL", maxUrlLength))
              .append(NL);
        for (Bookmark bookmark : bookmarks) {
            buffer.append(" ").append(padRight(bookmark.nChildren(), 14))
                .append(" ").append(padRight(bookmark.nDesc(), 14))
                .append(" ").append(padRight(bookmark.sDesc(), maxDescLength - 1))
                .append(" ").append(padRight(bookmark.nURL(), 14))
                .append(" ").append(bookmark.sURL())
                .append(NL);
        }

        return buffer.toString();
    }

    public List<Bookmark> getBookmarks() {
        return bookmarks;
    }
}
