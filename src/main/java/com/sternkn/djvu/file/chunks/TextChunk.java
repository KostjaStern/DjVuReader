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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.sternkn.djvu.utils.utils.InputStreamUtils.read24;
import static com.sternkn.djvu.utils.utils.InputStreamUtils.readString;
import static com.sternkn.djvu.utils.utils.StringUtils.NL;

public class TextChunk extends Chunk {
    private static final Logger LOG = LoggerFactory.getLogger(TextChunk.class);

    private int lenText;
    private String text;
    private int version;
    private List<TextZone> textZones;
    private int textZoneCount;

    public TextChunk(Chunk chunk) {
        super(chunk);
        decodeChunk();
    }

    private void decodeChunk() {
        try (InputStream stream = getInputStream()) {
            lenText = read24(stream);
            LOG.debug("lenText: {}", lenText);

            text = readString(stream, lenText);
            version = stream.read();
            LOG.debug("version: {}", version);

            textZones = new ArrayList<>();
            int typeCode = stream.read();
            int zoneId = 0;
            while (typeCode > 0) {
                TextZoneType type = TextZoneType.valueOf(typeCode);
                TextZone textZone = new TextZone(type, zoneId);

                zoneId = textZone.decode(stream, lenText, zoneId, null, null);

                textZones.add(textZone);
                typeCode = stream.read();
            }

            textZoneCount = zoneId + 1;
        }
        catch (IOException exception) {
            throw new DjVuFileException(exception.getMessage(), exception);
        }
    }

    private InputStream getInputStream() {
        InputStream stream = new ByteArrayInputStream(data);
        if (this.getChunkId() == ChunkId.TXTz) {
            stream = new BSByteInputStream(stream);
        }
        return stream;
    }

    @Override
    public String getDataAsText() {
        String parentData = super.getDataAsText();
        StringBuilder buffer = new StringBuilder();
        buffer.append(parentData).append(NL);
        buffer.append(" Version: ").append(version).append(NL);
        buffer.append(" Text zone count: ").append(textZoneCount).append(NL);
        buffer.append(" Size of the text string in bytes: ").append(lenText).append(NL);
        buffer.append(" Text: ").append(NL);
        buffer.append("--------------------------------------------------------").append(NL);
        buffer.append(text).append(NL).append(NL);

        return buffer.toString();
    }

    public int getTextZoneCount() {
        return textZoneCount;
    }

    public int getLenText() {
        return lenText;
    }

    public String getText() {
        return text;
    }

    public int getVersion() {
        return version;
    }

    public List<TextZone> getTextZones() {
        return textZones;
    }
}
