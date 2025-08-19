package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.DjVuFileException;
import com.sternkn.djvu.file.coders.BSByteInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.stream.IntStream;

import static com.sternkn.djvu.file.utils.InputStreamUtils.read16;
import static com.sternkn.djvu.file.utils.InputStreamUtils.read32;
import static com.sternkn.djvu.file.utils.InputStreamUtils.read24;
import static com.sternkn.djvu.file.utils.InputStreamUtils.readZeroTerminatedString;
import static com.sternkn.djvu.file.utils.StringUtils.NL;
import static com.sternkn.djvu.file.utils.StringUtils.padRight;
import static com.sternkn.djvu.file.utils.StringUtils.repeatString;

/*
  8.3.2 Directory Chunk: DIRM

  The first contained chunk in a FORM:DJVM composite chunk is the DIRM chunk containing the document directory.
  It contains information the decoder will need to access the component files (see Multipage Documents).
 */
public class DirectoryChunk extends Chunk {
    private static final Logger LOG = LoggerFactory.getLogger(DirectoryChunk.class);

    private static final int COLUMN_15 = 15;
    private static final int COLUMN_30 = 30;

    private final boolean isBundled;
    private final int version;
    private final int nFiles;

    private final List<ComponentInfo> components;

    public DirectoryChunk(Chunk chunk) {
        super(chunk);
        final ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
        int flags = byteStream.read();
        isBundled = (flags & 0x80) != 0;
        version = flags & 0x7f;
        nFiles = read16(byteStream);
        components = IntStream.range(0, nFiles).mapToObj(i -> new ComponentInfo()).toList();

        readComponents(byteStream);

        validateComponents();
    }

    private void validateComponents() {
        long sharedAnnotationsCount = components.stream()
            .filter(c -> c.getType() == ComponentType.SHARED_ANNO).count();
        if (sharedAnnotationsCount > 1) {
            throw new DjVuFileException("Directory chunk can not have more than one SHARED_ANNO component");
        }
    }

    public boolean isBundled() {
        return isBundled;
    }

    public int getVersion() {
        return version;
    }

    public int getNumberOfComponents() {
        return nFiles;
    }

    public List<ComponentInfo> getComponents() {
        return components;
    }

    @Override
    public String getDataAsText() {
        String parentData = super.getDataAsText();
        String tableLine = repeatString("-", 3 * (COLUMN_15 + COLUMN_30));

        StringBuilder buffer = new StringBuilder();
        buffer.append(parentData);
        buffer.append(" Version: ").append(version).append(NL);
        buffer.append(" IsBundled: ").append(isBundled).append(NL);
        buffer.append(" Number of components: ").append(nFiles).append(NL).append(NL);
        buffer.append(tableLine).append(NL);
        buffer.append(" ").append(padRight("offset", COLUMN_15))
              .append(" ").append(padRight("size", COLUMN_15))
              .append(" ").append(padRight("type", COLUMN_15))
              .append(" ").append(padRight("id", COLUMN_30))
              .append(" ").append(padRight("name", COLUMN_30))
              .append(" ").append(padRight("title", COLUMN_30));

        buffer.append(NL);
        buffer.append(tableLine).append(NL);
        for (ComponentInfo component : getComponents()) {
            buffer.append(" ").append(padRight(component.getOffset(), COLUMN_15))
                  .append(" ").append(padRight(component.getSize(), COLUMN_15))
                  .append(" ").append(padRight(component.getType(), COLUMN_15))
                  .append(" ").append(padRight(component.getId(), COLUMN_30));

            if (component.hasName()) {
                buffer.append(" ").append(padRight(component.getName(), COLUMN_30));
            }
            if (component.hasTitle()) {
                buffer.append(" ").append(padRight(component.getTitle(), COLUMN_30));
            }

            buffer.append(NL);
        }

        return buffer.toString();
    }

    private void readComponents(ByteArrayInputStream byteStream) {
        if (isBundled) {
            for (int ind = 0; ind < nFiles; ind++) {
                components.get(ind).setOffset(read32(byteStream));
            }
        }

        final BSByteInputStream bzzData = new BSByteInputStream(byteStream);

        for (int ind = 0; ind < nFiles; ind++) {
            components.get(ind).setSize(read24(bzzData));
        }

        for (int ind = 0; ind < nFiles; ind++) {
            components.get(ind).setFlag(bzzData.read());
        }

        for (int ind = 0; ind < nFiles; ind++) {
            final ComponentInfo component = components.get(ind);
            component.setId(readZeroTerminatedString(bzzData));
            if (component.hasName()) {
                component.setName(readZeroTerminatedString(bzzData));
            }
            if (component.hasTitle()) {
                component.setTitle(readZeroTerminatedString(bzzData));
            }
        }
    }
}
