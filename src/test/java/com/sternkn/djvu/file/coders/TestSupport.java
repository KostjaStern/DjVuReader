package com.sternkn.djvu.file.coders;

import com.sternkn.djvu.file.chunks.Chunk;
import com.sternkn.djvu.file.chunks.ChunkId;

import java.io.IOException;
import java.io.InputStream;

public class TestSupport {
    private final ClassLoader classLoader = getClass().getClassLoader();
    private static final String PATH_CHUNKS = "test_chunks/";

    public InputStream readStream(String fileName) {
        return classLoader.getResourceAsStream(PATH_CHUNKS + fileName);
    }

    public byte[] readByteBuffer(String fileName) {
        try (InputStream inputStream = classLoader.getResourceAsStream(PATH_CHUNKS + fileName)) {
            return inputStream != null ? inputStream.readAllBytes() : new byte[0];
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Chunk readChunk(String fileName, ChunkId chunkId) {
        return readChunk(fileName, chunkId, null, 0L);
    }

    public Chunk readChunk(String fileName, ChunkId chunkId, Chunk parent, Long offsetStart) {
        byte[] buffer = readByteBuffer(fileName);

        return Chunk.builder()
                .withChunkId(chunkId)
                .withData(buffer)
                .withParent(parent)
                .withOffsetStart(offsetStart)
                .withSize(buffer.length).build();
    }

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
