package com.sternkn.djvu.file.chunks;

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

/*
  8.3.2 Directory Chunk: DIRM

  The first contained chunk in a FORM:DJVM composite chunk is the DIRM chunk containing the document directory.
  It contains information the decoder will need to access the component files (see Multipage Documents).
 */
public class DirectoryChunk extends Chunk {
    private static final Logger LOG = LoggerFactory.getLogger(DirectoryChunk.class);

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

        StringBuilder buffer = new StringBuilder();
        buffer.append(parentData);
        buffer.append(" Version: ").append(version).append(NL);
        buffer.append(" IsBundled: ").append(isBundled).append(NL);
        buffer.append(" Number of components: ").append(nFiles).append(NL).append(NL);
        buffer.append("-----------------------------------------------------------------").append(NL);
        buffer.append(" ").append(padRight("offset", 15))
              .append(" ").append(padRight("size", 15))
              .append(" ").append(padRight("type", 10))
              .append(" ").append(padRight("id", 30))
              .append(" ").append(padRight("name", 30))
              .append(" ").append(padRight("title", 30));

        buffer.append(NL);
        buffer.append("-----------------------------------------------------------------").append(NL);
        for (ComponentInfo component : getComponents()) {
            buffer.append(" ").append(padRight(component.getOffset(), 15))
                  .append(" ").append(padRight(component.getSize(), 15))
                  .append(" ").append(padRight(component.getType(), 10))
                  .append(" ").append(padRight(component.getId(), 30));

            if (component.hasName()) {
                buffer.append(" ").append(padRight(component.getName(), 30));
            }
            if (component.hasTitle()) {
                buffer.append(" ").append(padRight(component.getTitle(), 30));
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
