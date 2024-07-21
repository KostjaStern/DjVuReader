package com.sternkn.djvu;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class Main {

    public static void main(String ... args) throws IOException {
        System.out.println("Run ...");
        File djvuFile = new File("./test_files/Abert_Mozart_book__1.djvu");
        System.out.println("djvuFile.exists() = " + djvuFile.exists());
        System.out.println("djvuFile.length() = " + djvuFile.length());

        try (DataInputStream inputStream = new DataInputStream(new FileInputStream(djvuFile))) {
            String djvuHeader = readHeader(inputStream);
            System.out.println("djvuHeader = " + djvuHeader);

            String chunkId = readChunkId(inputStream);  // FORM
            System.out.println("chunkId = " + chunkId);

            int chunkSize = readChunkSize(inputStream);  // 29451857 bytes
            System.out.println("chunkSize = " + chunkSize);

            String secondaryId = readChunkId(inputStream); // DJVM
            System.out.println("secondaryId = " + secondaryId);

            String chunkId1 = readChunkId(inputStream); // DIRM
            System.out.println("chunkId1 = " + chunkId1);

            int dirmChunkSize = readChunkSize(inputStream);  //
            System.out.println("dirmChunkSize = " + dirmChunkSize); // 4527

            int test = 0x80;

            byte dirmFlags = inputStream.readByte();
            System.out.println("dirmFlags = " + dirmFlags); // -127

            int test1 = test & dirmFlags;
            System.out.println("test = " + test);
            System.out.println("test1 = " + test1);

            // the next two bytes of this input stream, interpreted as a signed 16-bit number
            int nFiles = inputStream.readShort();
            System.out.println("nFiles = " + nFiles); // 588
        }

    }

    // 41 54 26 54 -> AT&T; magic described in 8.1
    private static String readHeader(DataInputStream inputStream) throws IOException {
        byte[] header = new byte[4];
        int numberBites = inputStream.read(header);
        System.out.println("numberBites = " + numberBites);
        return new String(header);
    }

    private static String readChunkId(DataInputStream inputStream) throws IOException {
        byte[] chunkId = new byte[4];
        int numberBites = inputStream.read(chunkId);
        System.out.println("numberBites = " + numberBites);
        return new String(chunkId);
    }

    private static int readChunkSize(DataInputStream inputStream) throws IOException {
        return inputStream.readInt();
    }
}
