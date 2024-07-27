package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.DjVuFileException;
import com.sternkn.djvu.file.DjVuFileReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/*
  13 Appendix 4: BZZ coding
  https://codesearch.isocpp.org/actcd19/main/d/djvulibre/djvulibre_3.5.27.1-10/libdjvu/BSByteStream.cpp
 */
public class DirectoryChunk extends Chunk {

    private final boolean isBundled;
    private final int version;
    private final int nFiles;
    private final List<Integer> offsets;
    private final int bzzDataSize;
    private final byte[] bzzData;


    public DirectoryChunk(ChunkId chunkId, DjVuFileReader fileReader) {
        super(chunkId, fileReader.readChunkLength());
        // fileReader.readChunkLength() -> 00 00 11 AF

        byte dirmFlags = fileReader.readByte();
        System.out.println("dirmFlags = " + dirmFlags); // -127 -> 81

        this.isBundled = (dirmFlags & 0b1000_0000) == 0b1000_0000;
        this.version = dirmFlags & 0b0111_1111;

        // the next two bytes of this input stream, interpreted as a signed 16-bit number
        this.nFiles = fileReader.readShort(); // 02 4C

        if (this.isBundled) {
            this.offsets = new ArrayList<>(this.nFiles);
            for (int ind = 0; ind < this.nFiles; ind++) {
                this.offsets.add(fileReader.readInt());
            }
        }
        else {
            this.offsets = Collections.emptyList();
        }
        logOffsets();

        this.bzzDataSize = this.getLength() - this.nFiles * 4 - 2;
        this.bzzData = new byte[this.bzzDataSize];
        int numberOfBytesRead = fileReader.readBytes(this.bzzData);

        if (numberOfBytesRead < this.bzzDataSize) {
            throw new DjVuFileException("Unexpected end of " + this.getChunkId() + " chunk after reading " +
                    numberOfBytesRead + " bytes");
        }

        logBzzData();
    }

    private void logOffsets() {
        final int size = this.offsets.size();
        System.out.println("------   offsets     ------");
        if (size < 6) {
            for (int ind = 0; ind < size; ind++) {
                System.out.println("offsets[" + ind + "] = " + this.offsets.get(ind));
            }
        }
        else {
            System.out.println("offsets[0] = " + this.offsets.get(0));
            System.out.println("offsets[1] = " + this.offsets.get(1));
            System.out.println("offsets[2] = " + this.offsets.get(2));
            System.out.println(".........................");
            System.out.println("offsets[" + (size - 3) + "] = " + this.offsets.get(size - 3));
            System.out.println("offsets[" + (size - 2) + "] = " + this.offsets.get(size - 2));
            System.out.println("offsets[" + (size - 1) + "] = " + this.offsets.get(size - 1));
        }
        System.out.println("-------------------------------");
    }

    private void logBzzData() {
        System.out.println("------   bzzData     ------");
        System.out.println("this.bzzDataSize = " + this.bzzDataSize);

        if (this.bzzDataSize < 10) {
            for (int ind = 0; ind < this.bzzDataSize; ind++) {
                System.out.println("bzzData[" + ind + "] = " + bzzData[ind]);
            }
        }
        else {
            System.out.println("bzzData[0] = " + bzzData[0]);
            System.out.println("bzzData[1] = " + bzzData[1]);
            System.out.println("bzzData[2] = " + bzzData[2]);
            System.out.println("bzzData[3] = " + bzzData[3]);
            System.out.println("bzzData[4] = " + bzzData[4]);
            System.out.println(".........................");
            System.out.println("bzzData[" + (bzzDataSize - 5) + "] = " + bzzData[bzzDataSize - 5]);
            System.out.println("bzzData[" + (bzzDataSize - 4) + "] = " + bzzData[bzzDataSize - 4]);
            System.out.println("bzzData[" + (bzzDataSize - 3) + "] = " + bzzData[bzzDataSize - 3]);
            System.out.println("bzzData[" + (bzzDataSize - 2) + "] = " + bzzData[bzzDataSize - 2]);
            System.out.println("bzzData[" + (bzzDataSize - 1) + "] = " + bzzData[bzzDataSize - 1]);
        }
        System.out.println("---------------------");
    }

    @Override
    public String toString() {
        return "DirectoryChunk{chunkId = " + this.getChunkId()
                + ", length = " + this.getLength()
                + ", isBundled = " +  isBundled
                + ", version = " + version
                + ", nFiles = " + nFiles + "}";
    }
}
