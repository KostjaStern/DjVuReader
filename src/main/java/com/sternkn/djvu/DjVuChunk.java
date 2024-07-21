package com.sternkn.djvu;

import java.io.DataInputStream;
import java.io.IOException;

public abstract class DjVuChunk {

    private ChunkId chunkId;
    private int length;
    private DataInputStream inputStream;



    protected String readFourBytesString() {
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
