package com.sternkn.djvu.file;

import com.sternkn.djvu.file.chunks.ChunkId;
import com.sternkn.djvu.file.chunks.SecondaryChunkId;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DjVuFileReader implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(DjVuFileReader.class);

    private DataInputStream inputStream;
    private long position;

    public DjVuFileReader(File file) {
        open(file);
        this.position = 0;
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

    public long getPosition() {
        return position;
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
        return readInt();
    }

    public int readBytes(byte[] buffer) {
        int result = 0;
        try {
            result = inputStream.read(buffer);
        }
        catch (IOException e) {
            throw new DjVuFileException("Bytes reading problem", e);
        }

        if (result < 0) {
            throw new DjVuFileException("There is no more data because the end of the stream has been reached.");
        }
        else {
            this.position += result;
            LOG.debug("Current offset: {}, {} bytes were read", this.position, result);
        }
        return result;
    }

    public byte readByte() {
        try {
            byte result = inputStream.readByte();
            this.position += 1;
            LOG.debug("Current offset: {}, 1 byte was read", this.position);
            return result;
        }
        catch (IOException e) {
            throw new DjVuFileException("Byte reading problem", e);
        }
    }

    public short readShort() {
        try {
            short result = inputStream.readShort();
            this.position += 2;
            LOG.debug("Current offset: {}, 2 bytes were read", this.position);
            return result;
        }
        catch (IOException e) {
            throw new DjVuFileException("Int16 reading problem", e);
        }
    }

    public int readInt() {
        try {
            int result = inputStream.readInt();
            this.position += 4;
            LOG.debug("Current offset: {}, 4 bytes were read", this.position);
            return result;
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
            this.position += numberBites;
            LOG.debug("Current offset: {}, {} bytes were read", this.position, numberBites);
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
