package com.sternkn.djvu.file;

import com.sternkn.djvu.file.chunks.ChunkId;
import com.sternkn.djvu.file.chunks.DirectoryChunk;
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

        ChunkId chunkId = this.fileReader.readChunkId();  // FORM -> 46 4F 52 4D
        System.out.println("chunkId = " + chunkId);

        int chunkSize = this.fileReader.readChunkLength();  // 29451857 bytes -> 01 C1 66 51
        System.out.println("chunkSize = " + chunkSize);

        SecondaryChunkId secondaryId = this.fileReader.readSecondaryChunkId(); // DJVM -> 44 4A 56 AD
        System.out.println("secondaryId = " + secondaryId);

        ChunkId dirmChunkId = this.fileReader.readChunkId(); // DIRM
        System.out.println("dirmChunkId = " + dirmChunkId);

        DirectoryChunk directoryChunk = new DirectoryChunk(dirmChunkId, this.fileReader);
        System.out.println("directoryChunk = " + directoryChunk);

//        int dirmChunkSize = this.fileReader.readChunkLength();  //
//        System.out.println("dirmChunkSize = " + dirmChunkSize); // 4527

    }

}
