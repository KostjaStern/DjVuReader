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

import com.sternkn.djvu.file.DjVuFileException;
import com.sternkn.djvu.file.coders.BSByteInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static com.sternkn.djvu.utils.utils.InputStreamUtils.read16;
import static com.sternkn.djvu.utils.utils.InputStreamUtils.read24;
import static com.sternkn.djvu.utils.utils.StringUtils.NL;

public class FGbzChunk extends Chunk {
    private static final Logger LOG = LoggerFactory.getLogger(FGbzChunk.class);

    private static final int MAX_PALETTE_SIZE = 65535;
    private static final int DJVU_PALETTE_VERSION = 0;

    private final boolean isShapeTableExist;
    private final int version;
    private final int paletteSize;
    private final List<Color> colors;

    private final int dataSize;
    private final List<Integer> indexes;

    public FGbzChunk(Chunk chunk) {
        super(chunk);
        final ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
        int flags = byteStream.read();
        isShapeTableExist = (flags & 0x80) != 0;
        version = flags & 0x7f;
        LOG.debug("version = {}", version);
        LOG.debug("isShapeTableExist = {}", isShapeTableExist);

        if (version != DJVU_PALETTE_VERSION) {
            throw new DjVuFileException("DjVuPalette.bad_version");
        }

        paletteSize = read16(byteStream);
        LOG.debug("paletteSize = {}", paletteSize);
        if (paletteSize < 0 || paletteSize > MAX_PALETTE_SIZE) {
            throw new DjVuFileException("DjVuPalette.bad_palette");
        }

        colors = new ArrayList<>();
        for (int i = 0; i < paletteSize; i++) {
            colors.add(readColor(byteStream));
        }

        if (isShapeTableExist) {
            dataSize  = read24(byteStream);
            indexes = new ArrayList<>(dataSize);
            final BSByteInputStream bzzData = new BSByteInputStream(byteStream);
            for (int ind = 0; ind < dataSize; ind++) {
                indexes.add(read16(bzzData));
            }
        }
        else {
            dataSize = 0;
            indexes = List.of();
        }
        LOG.debug("dataSize = {}", dataSize);
    }

    @Override
    public String getDataAsText() {
        String parentData = super.getDataAsText();

        StringBuilder buffer = new StringBuilder();
        buffer.append(parentData).append(NL);
        buffer.append(" Version: ").append(version).append(NL);
        buffer.append(" Palette size: ").append(paletteSize).append(NL);
        if (paletteSize > 0) {
            buffer.append("------------------------------------").append(NL);
            for (Color color : colors) {
                buffer.append(" ").append(color).append(NL);
            }
            buffer.append(NL);
        }

        buffer.append(" Data size: ").append(dataSize).append(NL);

        StringBuilder indexBuffer = new StringBuilder();
        for (int ind = 0; ind < indexes.size(); ind++) {
            indexBuffer.append(indexes.get(ind));
            if (ind < indexes.size() - 1) {
                indexBuffer.append(", ");
            }
            if (ind > 0 && ind % 100 == 0) {
                indexBuffer.append(NL);
            }
        }
        buffer.append(" Indexes: ").append(indexBuffer).append(NL);

        return buffer.toString();
    }

        private Color readColor(ByteArrayInputStream byteStream) {
        return new Color(byteStream.read(), byteStream.read(), byteStream.read());
    }

    public int getVersion() {
        return version;
    }

    public int getPaletteSize() {
        return paletteSize;
    }

    public List<Color> getColors() {
        return colors;
    }

    public int getDataSize() {
        return dataSize;
    }

    public List<Integer> getIndexes() {
        return indexes;
    }
}
