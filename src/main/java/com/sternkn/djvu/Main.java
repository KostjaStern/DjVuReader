package com.sternkn.djvu;

import com.sternkn.djvu.file.DjVuFile;
import com.sternkn.djvu.file.DjVuFileReader;
import com.sternkn.djvu.file.chunks.Chunk;
import com.sternkn.djvu.file.chunks.ChunkId;
import com.sternkn.djvu.file.coders.BSByteInputStream;
import com.sternkn.djvu.file.coders.ZpCodecInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.color.ICC_ColorSpace;
import java.io.File;
// import java.io.FileNotFoundException;
// import java.io.FileOutputStream;
// import java.io.IOException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// xxd Abert_Mozart_book__1.djvu | less
// chunkIds = [INFO, CIDa, Djbz, TXTz, FORM, ANTz, BG44, INCL, Sjbz, DIRM]
// Djbz - Shared shape table.
// 8.3.11 Document Info Chunk: INFO
// 8.3.6 Bitonal Mask Chunk: Sjbz
// 8.3.5 Text Chunk: TXTa, TXTz
//                Text is contained in “TXTa” or “TXTz” chunks. The “TXTa” chunks contain the text
//                unencoded. The “ANTz” chunks contain the same information compressed with the BZZ
//                encoder (see BSByteStream.h).
// 8.3.4 Annotation Chunk: ANTa, ANTz
// 8.3.2 Directory Chunk: DIRM
//                The first part of the “DIRM” chunk consists is unencoded
//                The rest of the chunk is entirely compressed with the BZZ general purpose compressor
// 8.3.12 INCL
//                This is the counterpart to the FORM:DjVi chunk which provides document-level
//                (“shared”) information. The INCL chunk simply contains the (unencoded) UTF8
//                encoded ID of the included component file. To obtain the data for this chunk, the
//                decoder should look for this ID at in the governing DIRM chunk. The corresponding
//                chunk must be of type FORM:DJVI and contain the shared chunk.


public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);


    public static void main(String ... args) {
        LOG.info("Run ...");

        File file = new File("./test_files/ANTz_293.bzz"); // ANTz_137.bzz
        LOG.info("file.exists() = {}", file.exists());
        LOG.info("file.length() = {}", file.length());

        try (FileInputStream inputStream = new FileInputStream(file)) {
            // ZpCodecInputStream zpStream = new ZpCodecInputStream(inputStream);

            BSByteInputStream bsByteInputStream = new BSByteInputStream(inputStream);

            byte[] bytes = new byte[500];
            int length = bsByteInputStream.read(bytes);


            // int blocksize = decode_raw(zpStream, 24);
            // int decoder = zpStream.decoder();
            // System.out.println("blocksize = " + blocksize);
            System.out.println("length = " + length);
            System.out.println("string = " + new String(bytes, 0, length, StandardCharsets.UTF_8));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

  /*
        File file = new File("./test_files/Abert_Mozart_book__1.djvu");
        LOG.info("djvuFile.exists() = {}", file.exists());
        LOG.info("djvuFile.length() = {}", file.length());

        try (DjVuFileReader reader = new DjVuFileReader(file)) {

            DjVuFile djvuFile = reader.readFile();
            List<Chunk> chunks = djvuFile.getChunks();
            System.out.println("chunks.size() = " + chunks.size());

            Set<ChunkId> chunkIds = chunks.stream().map(Chunk::getChunkId).collect(Collectors.toSet());
            System.out.println("chunkIds = " + chunkIds);

//            for (Chunk chunk : chunks) {
//                ChunkId chunkId = chunk.getChunkId();
//                if (chunkId != ChunkId.ANTz && chunkId != ChunkId.TXTz) {
//                    continue;
//                }
//                File bzzFile = new File(chunkId + "_" + chunk.getId() + ".bzz");
//                try (FileOutputStream outputStream = new FileOutputStream(bzzFile)) {
//                    outputStream.write(chunk.getData().readAllBytes());
//                }
//                catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
        }
*/
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

    private static int decode_raw(ZpCodecInputStream zp, int bits) {
        int n = 1;
        final int m = (1 << bits);
        while (n < m)
        {
            final int b = zp.decoder();
            n = (n << 1) | b;
        }

        return n - m;
    }
}
