package com.sternkn.djvu.file.chunks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

import static com.sternkn.djvu.file.utils.InputStreamUtils.readString;
import static com.sternkn.djvu.file.utils.StringUtils.NL;

public class InclChunk extends Chunk {
    private static final Logger LOG = LoggerFactory.getLogger(InclChunk.class);

    private final String sharedComponentID;

    public InclChunk(Chunk chunk) {
        super(chunk);
        final ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
        this.sharedComponentID = readString(byteStream, data.length);
        LOG.debug("Shared component ID: {}", sharedComponentID);
    }

    public String getSharedComponentID() {
        return sharedComponentID;
    }

    @Override
    public String getDataAsText() {
        String parentData = super.getDataAsText();
        StringBuilder buffer = new StringBuilder();
        buffer.append(parentData).append(NL);
        buffer.append(" Shared component ID: ").append(sharedComponentID).append(NL);

        return buffer.toString();
    }
}
