package com.sternkn.djvu.file;

import com.sternkn.djvu.file.chunks.ChunkId;
import com.sternkn.djvu.file.chunks.SecondaryChunkId;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class DjVuFileReader implements Closeable {

    private DataInputStream inputStream;

    public DjVuFileReader(File file) {
        open(file);
    }

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
    }

    private void open(File file) {
        try {
            this.inputStream = new DataInputStream(new FileInputStream(file));
        }
        catch (IOException e) {
            throw new DjVuFileException(String.format("Can not open stream for file %s", file.getAbsolutePath()), e);
        }
    }

    public ChunkId readChunkId() {
        final String value = readFourBytesString();
        try {
            return ChunkId.valueOf(value);
        }
        catch (IllegalArgumentException e) {
            throw new DjVuFileException(String.format("Unexpected chunk id: %s", value), e);
        }
    }

    public SecondaryChunkId readSecondaryChunkId() {
        final String value = readFourBytesString();
        try {
            return SecondaryChunkId.valueOf(value);
        }
        catch (IllegalArgumentException e) {
            throw new DjVuFileException(String.format("Unexpected secondary chunk id: %s", value), e);
        }
    }

    public int readChunkLength() {
        try {
            return inputStream.readInt();
        }
        catch (IOException e) {
            throw new DjVuFileException("Chunk length reading problem", e);
        }
    }

    public int readBytes(byte[] buffer) {
        try {
            return inputStream.read(buffer);
        }
        catch (IOException e) {
            throw new DjVuFileException("Bytes reading problem", e);
        }
    }

    public byte readByte() {
        try {
            return inputStream.readByte();
        }
        catch (IOException e) {
            throw new DjVuFileException("Byte reading problem", e);
        }
    }

    public short readShort() {
        try {
            return inputStream.readShort();
        }
        catch (IOException e) {
            throw new DjVuFileException("Int16 reading problem", e);
        }
    }

    public int readInt() {
        try {
            return inputStream.readInt();
        }
        catch (IOException e) {
            throw new DjVuFileException("Int32 reading problem", e);
        }
    }

    public String readHeader() {
        return readFourBytesString();
    }

    private String readFourBytesString() {
        byte[] bytes = new byte[4];
        int numberBites = 0;

        try {
            numberBites = inputStream.read(bytes);
        }
        catch (IOException e) {
            throw new DjVuFileException("String token reading problem", e);
        }

        if (numberBites < bytes.length) {
            throw new DjVuFileException(
                    String.format("It was unexpected end of file after reading %d bytes from %d",
                            numberBites, bytes.length));
        }

        return new String(bytes);
    }
}
