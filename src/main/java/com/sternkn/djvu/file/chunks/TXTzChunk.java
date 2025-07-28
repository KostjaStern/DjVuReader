package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.coders.BSByteInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static com.sternkn.djvu.file.utils.InputStreamUtils.read24;
import static com.sternkn.djvu.file.utils.InputStreamUtils.readString;
import static com.sternkn.djvu.file.utils.StringUtils.NL;

public class TXTzChunk extends Chunk {
    private static final Logger LOG = LoggerFactory.getLogger(TXTzChunk.class);

    private final int lenText;
    private final String text;
    private final int version;
    private final List<TextZone> textZones;
    private final int textZoneCount;

    public TXTzChunk(Chunk chunk) {
        super(chunk);
        final ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
        final BSByteInputStream bzzData = new BSByteInputStream(byteStream);

        lenText  = read24(bzzData);
        LOG.debug("lenText: {}", lenText);

        text = readString(bzzData, lenText);
        version = bzzData.read();
        LOG.debug("version: {}", version);

        textZones  = new ArrayList<>();
        int typeCode = bzzData.read();
        int zoneId = 0;
        while (typeCode > 0) {
            TextZoneType type = TextZoneType.valueOf(typeCode);
            TextZone textZone = new TextZone(type, zoneId);

            zoneId = textZone.decode(bzzData, lenText, zoneId, null, null);

            textZones.add(textZone);
            typeCode = bzzData.read();
        }

        textZoneCount = zoneId + 1;
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
