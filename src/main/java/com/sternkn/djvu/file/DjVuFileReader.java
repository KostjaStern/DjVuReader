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
package com.sternkn.djvu.file;

import com.sternkn.djvu.file.chunks.Chunk;
import com.sternkn.djvu.file.chunks.ChunkId;
import com.sternkn.djvu.file.chunks.SecondaryChunkId;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DjVuFileReader implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(DjVuFileReader.class);

    private final File file;
    private final long fileSize;
    private DataInputStream inputStream;
    private long rawOffset;
    private boolean isEndOfFile;


    public DjVuFileReader(File file) {
        this.file = file;
        this.fileSize = file.length();
        open();
        this.rawOffset = 0;
        this.isEndOfFile = false;
    }

    public DjVuFile readFile() {
        readHeader();
        final List<Chunk> chunks = readChunks();
        return new DjVuFileImpl(chunks);
    }

    private List<Chunk> readChunks() {
        List<Chunk> chunks = new ArrayList<>();
        long chunkId = 0;

        Stack<Chunk> parentChunks = new Stack<>();
        while (!isEndOfFile) {
            final Chunk chunk = readChunk(parentChunks, chunkId);
            chunks.add(chunk);
            updateParentChunks(parentChunks, chunk);

            chunkId++;
            if (isLastChunk(chunk)) {
                isEndOfFile = true;
            }
        }

        return chunks;
    }

    private boolean isLastChunk(Chunk chunk) {
        Objects.requireNonNull(chunk, "chunk can not be null");

        return !chunk.isComposite() && chunk.getOffsetEnd() == fileSize;
    }

    private void updateParentChunks(Stack<Chunk> parentChunks, Chunk chunk) {
        Objects.requireNonNull(chunk, "chunk can not be null");
        Objects.requireNonNull(parentChunks, "parent chunks stack can not be null");

        if (chunk.isComposite()) {
            parentChunks.push(chunk);
            return;
        }
        if (parentChunks.isEmpty()) {
            return;
        }

        Chunk lastParent = parentChunks.peek();
        if (lastParent.getOffsetEnd() == chunk.getOffsetEnd()) {
            parentChunks.pop();
        }
    }

    private void alignOffset() {
        if (rawOffset % 2 == 1) {
            skipBytes(1);
        }
    }

    private void skipBytes(int size) {
        try {
            inputStream.skipBytes(size);
            rawOffset += size;
        }
        catch (IOException e) {
            throw new DjVuFileException(String.format("Can not skip %s bytes", size), e);
        }
    }

    private Chunk readChunk(Stack<Chunk> parentChunks, long id) {
        alignOffset(); // Skip padding byte

        final ChunkId chunkId = readChunkId();
        final int size = this.readInt();
        final long offsetStart = rawOffset;
        final SecondaryChunkId secondaryChunkId = chunkId.isComposite() ? readSecondaryChunkId() : null;
        byte[] data = null;

        if (!chunkId.isComposite()) {
            data = new byte[size];
            readBytes(data);
        }

        Chunk parent = parentChunks.empty() ? null : parentChunks.peek();

        return Chunk.builder()
                .withId(id)
                .withChunkId(chunkId)
                .withSecondaryChunkId(secondaryChunkId)
                .withSize(size)
                .withOffsetStart(offsetStart)
                .withParent(parent)
                .withData(data)
                .build();
    }

    @Override
    public void close() {
        if (inputStream == null) {
            return;
        }

        try {
            inputStream.close();
        }
        catch (IOException e) {
            throw new DjVuFileException(String.format("Can not close stream for file %s", file.getAbsolutePath()), e);
        }
    }

    private void open() {
        try {
            this.inputStream = new DataInputStream(new FileInputStream(file));
        }
        catch (IOException e) {
            throw new DjVuFileException(String.format("Can not open stream for file %s", file.getAbsolutePath()), e);
        }
    }

    private ChunkId readChunkId() {
        final String value = readFourBytesString();
        try {
            return ChunkId.valueOf(value);
        }
        catch (IllegalArgumentException e) {
            throw new DjVuFileException(String.format("Unexpected chunk id: %s", value), e);
        }
    }

    private SecondaryChunkId readSecondaryChunkId() {
        final String value = readFourBytesString();
        try {
            return SecondaryChunkId.valueOf(value);
        }
        catch (IllegalArgumentException e) {
            throw new DjVuFileException(String.format("Unexpected secondary chunk id: %s", value), e);
        }
    }

    private int readBytes(byte[] buffer) {
        int result = 0;
        try {
            result = inputStream.read(buffer);
        }
        catch (IOException e) {
            throw new DjVuFileException("Bytes reading problem", e);
        }

        if (result < 0) {
            this.isEndOfFile = true;
            LOG.debug("readBytes: It's end of file.");
        }
        else {
            this.rawOffset += result;
            this.isEndOfFile = false;
        }
        return result;
    }

    private int readInt() {
        try {
            int result = inputStream.readInt();
            this.rawOffset += 4;
            this.isEndOfFile = false;
            return result;
        }
        catch (EOFException eof) {
            this.isEndOfFile = true;
            LOG.debug("readInt: It's end of file.");
            return 0;
        }
        catch (IOException io) {
            throw new DjVuFileException("Int32 reading problem", io);
        }
    }

    /**
     * @return magic file header: AT&T or SDJV
     */
    private MagicHeader readHeader() {
        final String header = readFourBytesString();
        return MagicHeader.of(header);
    }

    private String readFourBytesString() {
        byte[] bytes = new byte[4];
        int numberBites = readBytes(bytes);

        if (numberBites < bytes.length) {
            throw new DjVuFileException(
                    String.format("It was unexpected end of file after reading %d bytes from %d",
                            numberBites, bytes.length));
        }

        return new String(bytes);
    }
}
