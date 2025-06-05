package com.sternkn.djvu;

import com.sternkn.djvu.file.DjVuFile;
import com.sternkn.djvu.file.DjVuFileReader;
import com.sternkn.djvu.file.chunks.Chunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;


public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String ... args) {
        LOG.info("Run ...");
        File file = new File("./test_files/Abert_Mozart_book__1.djvu");
        LOG.info("djvuFile.exists() = {}", file.exists());
        LOG.info("djvuFile.length() = {}", file.length());

        try (DjVuFileReader reader = new DjVuFileReader(file)) {

            DjVuFile djvuFile = reader.readFile();
            List<Chunk> chunks = djvuFile.getChunks();
            System.out.println("chunks.size() = " + chunks.size());
        }

        // System.out.println("Integer.toHexString(256) = " + Integer.toHexString(256));
        //        int test1 = 1 << 1;
//        LOG.info("test1 = {}", test1);
//
//        int test10 = 1 << 10;
//        LOG.info("test10 = {}", test10);
//
//        int test24 = 1 << 24;
//        LOG.info("test24 = {}", test24);

/*
        // The signed left shift operator "<<" shifts a bit pattern to the left
        int test1 = 0b0000_0011 << 2;
        System.out.println("test1 = " + test1);

        // the signed right shift operator ">>" shifts a bit pattern to the right
        int test2 = 0b0000_0111 >> 1; // 0b0000_0111 = 1 + 2 + 4 = 7
        System.out.println("test2 = " + test2);

        // The unary bitwise complement operator "~" inverts a bit pattern;
        // it can be applied to any of the integral types, making every "0" a "1" and every "1" a "0"
        int test3 = ~ 0b0000_0111;
        System.out.println("test3 = " + test3);

 */
    }
}
