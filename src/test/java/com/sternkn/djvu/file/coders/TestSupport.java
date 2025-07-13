package com.sternkn.djvu.file.coders;

import java.io.IOException;
import java.io.InputStream;

public class TestSupport {
    private final ClassLoader classLoader = getClass().getClassLoader();
    private static final String PATH_CHUNKS = "test_chunks/";

    JB2Image readImage(String imageFileName) {
        return readImage(imageFileName, null);
    }

    JB2Image readImage(String imageFileName, String dictFileName) {
        JB2Dict dict = dictFileName != null ? readDictionary(dictFileName) : null;
        JB2Image image = new JB2Image(dict);

        try (InputStream inputStream = classLoader.getResourceAsStream(PATH_CHUNKS + imageFileName)) {
            JB2CodecDecoder decoder = new JB2CodecDecoder(inputStream);
            decoder.decode(image);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        return image;
    }

    JB2Dict readDictionary(String fileName) {
        JB2Dict dict = new JB2Dict();

        try (InputStream inputStream = classLoader.getResourceAsStream(PATH_CHUNKS + fileName)) {
            JB2CodecDecoder decoder = new JB2CodecDecoder(inputStream);
            decoder.decode(dict);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        return dict;
    }
}
