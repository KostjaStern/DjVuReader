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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

import static com.sternkn.djvu.utils.InputStreamUtils.readString;
import static com.sternkn.djvu.utils.StringUtils.NL;

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
