package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.chunks.annotations.Alignment;
import com.sternkn.djvu.file.chunks.annotations.AnnotationParser;
import com.sternkn.djvu.file.chunks.annotations.BackgroundColor;
import com.sternkn.djvu.file.chunks.annotations.InitialDisplayLevel;
import com.sternkn.djvu.file.chunks.annotations.InitialZoom;
import com.sternkn.djvu.file.chunks.annotations.MapArea;
import com.sternkn.djvu.file.coders.BSByteInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static com.sternkn.djvu.file.utils.InputStreamUtils.readString;
import static com.sternkn.djvu.file.utils.StringUtils.NL;

public class AnnotationChunk extends Chunk {
    private static final Logger LOG = LoggerFactory.getLogger(AnnotationChunk.class);

    private final String plainText;
    private final BackgroundColor backgroundColor;
    private final InitialZoom  initialZoom;
    private final InitialDisplayLevel initialDisplayLevel;
    private final Alignment alignment;
    private final List<MapArea>  mapAreas;

    public AnnotationChunk(Chunk chunk) {
        super(chunk);
        this.plainText = encodeText();
        LOG.debug("plainText = {}", plainText);

        AnnotationParser annotationParser = new AnnotationParser(this.plainText);
        backgroundColor =  annotationParser.getBackgroundColor();
        initialZoom = annotationParser.getInitialZoom();
        initialDisplayLevel = annotationParser.getInitialDisplayLevel();
        alignment = annotationParser.getAlignment();
        mapAreas = annotationParser.getMapAreas();
    }

    private String encodeText() {
        InputStream byteStream = new ByteArrayInputStream(data);
        if (this.getChunkId() == ChunkId.ANTz) {
            byteStream = new BSByteInputStream(byteStream);
        }
        return readString(byteStream);
    }

    public String getPlainText() {
        return plainText;
    }

    public BackgroundColor getBackgroundColor() {
        return backgroundColor;
    }

    public InitialZoom getInitialZoom() {
        return initialZoom;
    }

    public InitialDisplayLevel getInitialDisplayLevel() {
        return initialDisplayLevel;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public List<MapArea> getMapAreas() {
        return mapAreas;
    }

    @Override
    public String getDataAsText() {
        String parentData = super.getDataAsText();

        StringBuilder buffer = new StringBuilder();
        buffer.append(parentData).append(NL);

        buffer.append(" Plain text: ").append(plainText).append(NL).append(NL);
        buffer.append(" BackgroundColor: ").append(backgroundColor).append(NL);
        buffer.append(" InitialZoom: ").append(initialZoom).append(NL);
        buffer.append(" InitialDisplayLevel: ").append(initialDisplayLevel).append(NL);
        buffer.append(" Alignment: ").append(alignment).append(NL).append(NL);

        for (int index = 0; index < mapAreas.size(); index++) {
            MapArea mapArea = mapAreas.get(index);
            buffer.append(" MapArea[")
                  .append(index)
                  .append("]: ")
                  .append(mapArea).append(NL);
        }

        return  buffer.toString();
    }
}
