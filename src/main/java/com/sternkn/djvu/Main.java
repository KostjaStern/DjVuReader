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
import java.io.ByteArrayInputStream;
import java.io.File;
// import java.io.FileNotFoundException;
// import java.io.FileOutputStream;
// import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// xxd Abert_Mozart_book__1.djvu | less
// chunkIds = [INFO, CIDa, Djbz, TXTz, FORM, ANTz, BG44, INCL, Sjbz, DIRM]
// A “Sjbz” chunk contains the bilevel data encoded with the JB2 representation
// "BG44" chunks contain the image data encoded with the IW44 representation.
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

// REST API endpoints for commit statuses
// Use the REST API to interact with commit statuses.
// https://docs.github.com/en/rest/commits/statuses?apiVersion=2022-11-28

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);


    public static void main(String ... args) throws IOException {

        // sicilianskaya-zashchita-ataka-rauzera.djvu , Abert_Mozart_book__1.djvu
        File file = new File("./test_files/sample1.djvu");
        LOG.info("djvuFile.exists() = {}", file.exists());
        LOG.info("djvuFile.length() = {}", file.length());
        splitDjVuFileByChunks(file);
    }

    private static void splitDjVuFileByChunks(File file) {
        try (DjVuFileReader reader = new DjVuFileReader(file)) {

            DjVuFile djvuFile = reader.readFile();
            List<Chunk> chunks = djvuFile.getChunks();
            System.out.println("chunks.size() = " + chunks.size());

            Set<ChunkId> chunkIds = chunks.stream().map(Chunk::getChunkId).collect(Collectors.toSet());
            System.out.println("chunkIds = " + chunkIds);

            // chunkIds = [INFO, CIDa, Djbz, TXTz, FORM, ANTz, BG44, INCL, Sjbz, DIRM]
            // chunkIds = [DIRM, INFO, Sjbz, TXTz, FORM]

            Map<ChunkId, Integer> chunksCount = new HashMap<>();
            // chunksCount.put(ChunkId.INFO, 0);
            // chunksCount.put(ChunkId.CIDa, 0);
            // chunksCount.put(ChunkId.Djbz, 0);
            // chunksCount.put(ChunkId.BG44, 0);
            // chunksCount.put(ChunkId.INCL, 0);
            chunksCount.put(ChunkId.DIRM, 0);
            chunksCount.put(ChunkId.TXTz, 0);
            chunksCount.put(ChunkId.Sjbz, 0);

            for (Chunk chunk : chunks) {
                ChunkId chunkId = chunk.getChunkId();
                Integer count = chunksCount.get(chunkId);
                if (count == null || count > 10) {
                    continue;
                }

                chunksCount.put(chunkId, count + 1);

                File chunkFile = new File(chunkId + "_" + chunk.getId() + ".data");
                try (FileOutputStream outputStream = new FileOutputStream(chunkFile)) {

//                    ByteArrayInputStream data = chunk.getData();
//                    int version = data.read();
//                    byte[] int16 = new byte[2];
//                    data.read(int16);
//                    int nFiles = int16[0] << 8 | int16[1];
//
//                    System.out.println("version = " + version);
//                    System.out.println("nFiles = " + nFiles);
//
//                    byte[] int32 = new byte[4];
//                    for (int i = 0; i < nFiles; i++) {
//                        data.read(int32);
//                    }

                    outputStream.write(chunk.getData().readAllBytes());
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }
    }
}
