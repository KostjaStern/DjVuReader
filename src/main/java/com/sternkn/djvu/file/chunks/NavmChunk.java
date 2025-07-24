package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.coders.BSByteInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static com.sternkn.djvu.file.utils.InputStreamUtils.read16;
import static com.sternkn.djvu.file.utils.InputStreamUtils.read24;
import static com.sternkn.djvu.file.utils.InputStreamUtils.readString;

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

    public List<Bookmark> getBookmarks() {
        return bookmarks;
    }
}
