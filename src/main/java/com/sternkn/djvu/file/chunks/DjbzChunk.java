package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.DjVuFileException;
import com.sternkn.djvu.file.DjVuFileReader;

/*
   Shared shape table

   11.4.10.1 Shared Shape Dictionaries
 */
public class DjbzChunk extends Chunk {

    private final byte[] jb2;

    public DjbzChunk(ChunkId chunkId, DjVuFileReader fileReader) {
        super(chunkId, fileReader.readChunkLength());

        this.jb2 = new byte[this.getLength() + 1];

        int numberOfBytesRead = fileReader.readBytes(this.jb2);

        if (numberOfBytesRead < this.jb2.length) {
            throw new DjVuFileException("Unexpected end of " + this.getChunkId() + " chunk after reading " +
                    numberOfBytesRead + " bytes");
        }

        logJb2();
    }

    private void logJb2() {
        System.out.println("------   jb2     ------");
        final int jb2Size = this.jb2.length;
        System.out.println("this.jb2.length = " + jb2Size);

        if (jb2Size < 10) {
            for (int ind = 0; ind < jb2Size; ind++) {
                System.out.println("jb2[" + ind + "] = " + jb2[ind]);
            }
        }
        else {
            System.out.println("jb2[0] = " + jb2[0]);
            System.out.println("jb2[1] = " + jb2[1]);
            System.out.println("jb2[2] = " + jb2[2]);
            System.out.println("jb2[3] = " + jb2[3]);
            System.out.println("jb2[4] = " + jb2[4]);
            System.out.println(".........................");
            System.out.println("jb2[" + (jb2Size - 5) + "] = " + jb2[jb2Size - 5]);
            System.out.println("jb2[" + (jb2Size - 4) + "] = " + jb2[jb2Size - 4]);
            System.out.println("jb2[" + (jb2Size - 3) + "] = " + jb2[jb2Size - 3]);
            System.out.println("jb2[" + (jb2Size - 2) + "] = " + jb2[jb2Size - 2]);
            System.out.println("jb2[" + (jb2Size - 1) + "] = " + jb2[jb2Size - 1]);
        }
        System.out.println("---------------------");
    }

    @Override
    public String toString() {
        return "DjbzChunk{chunkId = " + this.getChunkId()
                + ", length = " + this.getLength() + "}";
    }
}
