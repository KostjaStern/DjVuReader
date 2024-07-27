package com.sternkn.djvu.file;

import com.sternkn.djvu.file.chunks.ChunkId;
import com.sternkn.djvu.file.chunks.DirectoryChunk;
import com.sternkn.djvu.file.chunks.DjbzChunk;
import com.sternkn.djvu.file.chunks.PageFormChunk;
import com.sternkn.djvu.file.chunks.SecondaryChunkId;
import com.sternkn.djvu.file.chunks.SharedFormChunk;

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


        ChunkId chunkId1 = this.fileReader.readChunkId();
        System.out.println("chunkId1 = " + chunkId1); // FORM

        SharedFormChunk sharedFormChunk = new SharedFormChunk(chunkId1, this.fileReader);
        System.out.println("sharedFormChunk = " + sharedFormChunk);

        ChunkId chunkId2 = this.fileReader.readChunkId(); // FORM
        System.out.println("chunkId2 = " + chunkId2);     // 4 bytes

        PageFormChunk pageFormChunk = new PageFormChunk(chunkId2, this.fileReader);
        System.out.println("pageFormChunk = " + pageFormChunk);

        System.out.println("pageFormChunk.getInfoChunk() = " + pageFormChunk.getInfoChunk());

//        int chunkLength2 = this.fileReader.readChunkLength(); // 1407719
//        System.out.println("chunkLength2 = " + chunkLength2); // 4 bytes
//
//        SecondaryChunkId secondaryChunkId2 = this.fileReader.readSecondaryChunkId(); // DJVU
//        System.out.println("secondaryChunkId2 = " + secondaryChunkId2); // 4 bytes
    }
}
