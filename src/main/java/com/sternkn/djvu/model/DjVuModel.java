package com.sternkn.djvu.model;

import com.sternkn.djvu.file.DjVuFile;
import com.sternkn.djvu.file.DjVuFileException;
import com.sternkn.djvu.file.chunks.AnnotationChunk;
import com.sternkn.djvu.file.chunks.Chunk;
import com.sternkn.djvu.file.chunks.ChunkId;
import com.sternkn.djvu.file.chunks.DirectoryChunk;
import com.sternkn.djvu.file.chunks.FGbzChunk;
import com.sternkn.djvu.file.chunks.InclChunk;
import com.sternkn.djvu.file.chunks.InfoChunk;
import com.sternkn.djvu.file.chunks.LTAnnotationChunk;
import com.sternkn.djvu.file.chunks.NavmChunk;
import com.sternkn.djvu.file.chunks.TextChunk;
import com.sternkn.djvu.file.coders.IW44Image;
import com.sternkn.djvu.file.coders.IW44SecondaryHeader;
import com.sternkn.djvu.file.coders.JB2CodecDecoder;
import com.sternkn.djvu.file.coders.JB2Dict;
import com.sternkn.djvu.file.coders.JB2Image;
import com.sternkn.djvu.file.coders.Pixmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.sternkn.djvu.file.utils.StringUtils.NL;
import static com.sternkn.djvu.file.utils.StringUtils.padRight;

public class DjVuModel {
    private static final Logger LOG = LoggerFactory.getLogger(DjVuModel.class);

    private final DjVuFile djvuFile;

    public DjVuModel(DjVuFile djvuFile) {
        this.djvuFile = djvuFile;
    }

    private Chunk getChunkById(long chunkId) {
        List<Chunk> chunks = this.djvuFile.getChunks().stream()
            .filter(c -> c.getId() == chunkId).toList();

        if (chunks.isEmpty()) {
            throw new DjVuFileException("Chunk with id " + chunkId + " not found");
        }

        if (chunks.size() > 1) {
            LOG.warn("More than one chunk with id {} were found", chunkId);
        }

        return chunks.getFirst();
    }

    public void saveChunkData(File file, long chunkId) {
        Chunk chunk = getChunkById(chunkId);

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(chunk.getData());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ChunkInfo getChunkInfo(long chunkId) {
        Chunk chunk = getChunkById(chunkId);
        ChunkId chunkType = chunk.getChunkId();

        if (chunkType == ChunkId.Sjbz) {
            return getBitonalChunkInfo(chunk);
        }

        if (chunkType.isIW44Chunk()) {
            return getIW44ChunkImage(chunk);
        }

        if (chunkType.isTextChunk()) {
            return getTextChunkInfo(chunk);
        }

        Chunk decodedChunk = switch (chunkType) {
            case ChunkId.DIRM -> new DirectoryChunk(chunk);
            case ChunkId.INFO -> new InfoChunk(chunk);
            case ChunkId.NAVM -> new NavmChunk(chunk);
            case ChunkId.INCL -> new InclChunk(chunk);
            case ChunkId.FGbz -> new FGbzChunk(chunk);
            case ChunkId.ANTz, ChunkId.ANTa -> new AnnotationChunk(chunk);
            case ChunkId.LTAz, ChunkId.LTAa -> new LTAnnotationChunk(chunk);
            default -> chunk;
        };

        return new ChunkInfo(chunk.getId())
            .setTextData(decodedChunk.getDataAsText());
    }

    public String getChunkStatistics() {
        Map<String, Long> compositeChunksStat = this.djvuFile.getChunks().stream()
            .filter(Chunk::isComposite)
            .map(Chunk::getCompositeChunkId)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Map<String, Long> dataChunksStat = this.djvuFile.getChunks().stream()
                .filter(c -> !c.isComposite())
                .map(c -> c.getChunkId().name())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        StringBuilder buffer = new StringBuilder();
        buffer.append("    Composite chunks  ").append(NL);
        buffer.append("---------------------------------").append(NL);
        for (Map.Entry<String, Long> entry : compositeChunksStat.entrySet()) {
            buffer.append(" ")
                  .append(padRight(entry.getKey(), 15))
                  .append(": ").append(entry.getValue()).append(NL);
        }
        buffer.append(NL).append(NL);
        buffer.append("    Data chunks  ").append(NL);
        buffer.append("---------------------------------").append(NL);
        for (Map.Entry<String, Long> entry : dataChunksStat.entrySet()) {
            buffer.append(" ")
                  .append(padRight(entry.getKey(), 15))
                  .append(": ").append(entry.getValue()).append(NL);
        }

        return buffer.toString();
    }

    private ChunkInfo getTextChunkInfo(Chunk chunk) {
        if (!chunk.getChunkId().isTextChunk()) {
            return null;
        }

        TextChunk textChunk = new TextChunk(chunk);

        return new ChunkInfo(chunk.getId())
            .setTextData(textChunk.getDataAsText())
            .setTextZoneCount(textChunk.getTextZoneCount())
            .setTextZones(textChunk.getTextZones());
    }

    private ChunkInfo getIW44ChunkImage(Chunk chunk) {
        List<Chunk> chunks = this.djvuFile.getAllImageChunks(chunk);

        IW44Image image = new IW44Image();
        chunks.forEach(ch -> image.decode_chunk(ch.getData()));
        image.close_codec();
        IW44SecondaryHeader header = image.getSecondaryHeader();

        Pixmap bitmap = image.get_pixmap();

        String text = String.format(
                """
                 %s
                 majorVersion = %s
                 minorVersion = %s
                 colorType = %s
                 chrominanceDelay = %s
                 crcbHalf = %s
                 height = %s
                 width = %s
                """, chunk.getDataAsText(),
                header.getMajorVersion(), header.getMinorVersion(), header.getColorType(),
                header.getChrominanceDelay(), header.getCrcbHalf(),
                bitmap.getHeight(),  bitmap.getWidth());

        return new ChunkInfo(chunk.getId())
            .setTextData(text)
            .setBitmap(bitmap);
    }

    /*
        https://habr.com/ru/articles/331618/ - Smoothing images with Peron and Malik's anisotropic diffusion filter
        Methods of Bitonal Image Conversion for Modern and Classic Documents
     */
    private ChunkInfo getBitonalChunkInfo(Chunk chunk) {
        Chunk sharedShape = this.djvuFile.findSharedShapeChunk(chunk);
        JB2Dict dict = null;
        if (sharedShape != null) {
            dict = new JB2Dict();
            JB2CodecDecoder decoder = new JB2CodecDecoder(new ByteArrayInputStream(sharedShape.getData()));
            decoder.decode(dict);
        }

        JB2Image image = new JB2Image(dict);
        JB2CodecDecoder decoder = new JB2CodecDecoder(new ByteArrayInputStream(chunk.getData()));
        decoder.decode(image);

        Pixmap bitmap = image.get_bitmap();

        return new ChunkInfo(chunk.getId())
            .setTextData(chunk.getDataAsText())
            .setBitmap(bitmap);
    }
}
