package com.sternkn.djvu.file;

import com.sternkn.djvu.file.chunks.ChunkId;
import com.sternkn.djvu.file.chunks.SecondaryChunkId;

import java.io.File;

public class DjVuFile {

    private String header;
    private long fileSize;

    private DjVuFileReader fileReader;

    public DjVuFile(File file) {
        this.fileSize = file.length();
        this.fileReader = new DjVuFileReader(file);
        this.header = this.fileReader.readHeader();

        System.out.println("fileSize = " + fileSize);
        System.out.println("header = " + header);

        ChunkId chunkId = this.fileReader.readChunkId();  // FORM
        System.out.println("chunkId = " + chunkId);

        int chunkSize = this.fileReader.readChunkLength();  // 29451857 bytes
        System.out.println("chunkSize = " + chunkSize);

        SecondaryChunkId secondaryId = this.fileReader.readSecondaryChunkId(); // DJVM
        System.out.println("secondaryId = " + secondaryId);

        ChunkId chunkId1 = this.fileReader.readChunkId(); // DIRM
        System.out.println("chunkId1 = " + chunkId1);

        int dirmChunkSize = this.fileReader.readChunkLength();  //
        System.out.println("dirmChunkSize = " + dirmChunkSize); // 4527

        int test = 0x80;

        byte dirmFlags = this.fileReader.readByte();
        System.out.println("dirmFlags = " + dirmFlags); // -127

        int test1 = test & dirmFlags;
        System.out.println("test = " + test);
        System.out.println("test1 = " + test1);

        // the next two bytes of this input stream, interpreted as a signed 16-bit number
        int nFiles = this.fileReader.readShort();
        System.out.println("nFiles = " + nFiles); // 588
    }

}
