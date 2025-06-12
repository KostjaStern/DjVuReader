package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.coders.BSByteInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.IntStream;

import static com.sternkn.djvu.file.utils.InputStreamUtils.read16;
import static com.sternkn.djvu.file.utils.InputStreamUtils.read32;
import static com.sternkn.djvu.file.utils.InputStreamUtils.read24;
import static com.sternkn.djvu.file.utils.InputStreamUtils.readZeroTerminatedString;

/*
  8.3.2 Directory Chunk: DIRM

  The first contained chunk in a FORM:DJVM composite chunk is the DIRM chunk containing the document directory.
  It contains information the decoder will need to access the component files (see Multipage Documents).

  13 Appendix 4: BZZ coding
  https://codesearch.isocpp.org/actcd19/main/d/djvulibre/djvulibre_3.5.27.1-10/libdjvu/BSByteStream.cpp

  https://github.com/traycold/djvulibre/blob/master/libdjvu/BSByteStream.cpp

  https://en.wikipedia.org/wiki/Burrows%E2%80%93Wheeler_transform
 */
public class DirectoryChunk extends Chunk {
    private static final Logger LOG = LoggerFactory.getLogger(DirectoryChunk.class);

    private final boolean isBundled;
    private final int version;
    private final int nFiles;

    private final List<ComponentInfo> components;


    public DirectoryChunk(Chunk chunk) {
        super(chunk);

        int flags = data.read();
        isBundled = (flags & 0x80) != 0;
        version = flags & 0x7f;
        nFiles = read16(data);
        components = IntStream.range(0, nFiles).mapToObj(i -> new ComponentInfo()).toList();

        readComponents();
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

    private void readComponents() {
        if (isBundled) {
            for (int ind = 0; ind < nFiles; ind++) {
                components.get(ind).setOffset(read32(data));
            }
        }

        final BSByteInputStream bzzData = new BSByteInputStream(data);

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
