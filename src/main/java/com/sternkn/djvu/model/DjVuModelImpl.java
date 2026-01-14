/*
    Copyright (C) 2025 Kostya Stern

    This program is free software; you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation; either version 2 of the License, or (at your option)
    any later version.

    This program is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
    more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc., 51
    Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
*/
package com.sternkn.djvu.model;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sternkn.djvu.file.DjVuFile;
import com.sternkn.djvu.file.chunks.AnnotationChunk;
import com.sternkn.djvu.file.chunks.Bookmark;
import com.sternkn.djvu.file.chunks.Chunk;
import com.sternkn.djvu.file.chunks.ChunkId;
import com.sternkn.djvu.file.chunks.ComponentType;
import com.sternkn.djvu.file.chunks.FGbzChunk;
import com.sternkn.djvu.file.chunks.ImageRotationType;
import com.sternkn.djvu.file.chunks.InclChunk;
import com.sternkn.djvu.file.chunks.InfoChunk;
import com.sternkn.djvu.file.chunks.LTAnnotationChunk;
import com.sternkn.djvu.file.chunks.NavmChunk;
import com.sternkn.djvu.file.chunks.TextChunk;
import com.sternkn.djvu.file.coders.IW44Image;
import com.sternkn.djvu.file.coders.IW44SecondaryHeader;
import com.sternkn.djvu.file.coders.JB2Image;
import com.sternkn.djvu.file.coders.Pixmap;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.sternkn.djvu.utils.ImageUtils.composeImage;
import static com.sternkn.djvu.utils.ImageUtils.createBlank;
import static com.sternkn.djvu.utils.ImageUtils.decodeJB2Image;
import static com.sternkn.djvu.utils.ImageUtils.decodeIW44Image;
import static com.sternkn.djvu.utils.StringUtils.NL;
import static com.sternkn.djvu.utils.StringUtils.padRight;

public class DjVuModelImpl implements DjVuModel {
    private static final Logger LOG = LoggerFactory.getLogger(DjVuModelImpl.class);

    private final DjVuFile djvuFile;
    private List<Page> pages;
    private List<MenuNode> menuNodes;
    private final Cache<Page, Image> pagesCache;

    public DjVuModelImpl(DjVuFile djvuFile) {
        this.djvuFile = djvuFile;
        pages = null;
        menuNodes = null;
        pagesCache = Caffeine.newBuilder()
            .maximumSize(5)
            .expireAfterWrite(Duration.ofMinutes(60))
            .build();
    }

    private List<Page> calculatePages() {
        return djvuFile.getDirectoryChunk().getComponents().stream()
            .filter(c -> c.getType() == ComponentType.PAGE)
            .map(c -> new Page(c.getOffset(), c.getId()))
            .toList();
    }

    private List<MenuNode> calculateMenuNodes() {
        List<MenuNode> nodes = new ArrayList<>();
        Optional<NavmChunk> menu = djvuFile.getNavigationMenu();
        if (menu.isEmpty()) {
            return nodes;
        }

        MenuNode root = new MenuNode("Root", null);
        nodes.add(root);
        List<Bookmark> bookmarks = menu.get().getBookmarks();
        final int bookmarksCount = bookmarks.size();

        int index = 0;

        while (index < bookmarksCount) {
            Bookmark bookmark = bookmarks.get(index);
            Integer pageNumber = calculatePageNumber(bookmark.sURL());

            MenuNode node = new MenuNode(bookmark.sDesc(), pageNumber);
            root.getChildren().add(node);
            nodes.add(node);
            index++;

            if (bookmark.nChildren() != 0) {
                index =  readChildren(node, nodes, bookmarks, index, bookmark.nChildren());
            }
        }

        return nodes;
    }

    private int readChildren(MenuNode parentNode, List<MenuNode> nodes, List<Bookmark> bookmarks, int currentIndex, int nChildren) {
        int index = currentIndex;
        int counter = 0;
        while (counter < nChildren) {
            Bookmark bookmark = bookmarks.get(index);
            Integer pageNumber = calculatePageNumber(bookmark.sURL());
            MenuNode node = new MenuNode(bookmark.sDesc(), pageNumber);
            nodes.add(node);
            parentNode.getChildren().add(node);

            index++;
            counter++;

            if (bookmark.nChildren() != 0) {
                index =  readChildren(node, nodes, bookmarks, index, bookmark.nChildren());
            }
        }

        return index;
    }

    private Integer calculatePageNumber(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        final String pageId = url.substring(1);

        try {
            return Integer.parseInt(pageId);
        }
        catch (NumberFormatException e) {
            List<Page> pages = getPages();
            return IntStream.range(0, pages.size())
                    .filter(i -> Objects.equals(pages.get(i).getId(), pageId))
                    .map(i -> i + 1)
                    .boxed()
                    .findFirst()
                    .orElse(null);
        }
    }

    @Override
    public List<Page> getPages() {
        if (pages == null) {
            pages = calculatePages();
        }

        return pages;
    }

    @Override
    public List<MenuNode> getMenuNodes() {
        if (menuNodes == null) {
            menuNodes = calculateMenuNodes();
        }

        return menuNodes;
    }

    @Override
    public Image getCachedPageImage(Page page) {
        return pagesCache.get(page, this::getPageImage);
    }

    @Override
    public Image getPageImage(Page page) {
        Chunk chunk = djvuFile.getChunkByOffset(page.getOffset());
        InfoChunk info = new InfoChunk(chunk);

        LOG.debug("Page offset = {}, info = {}", page.getOffset(), info);

        Map<ChunkId, List<Chunk>> pageChunks = djvuFile.getAllPageChunks(chunk);

        Chunk sjbz = getChunk(pageChunks, ChunkId.Sjbz);
        Chunk fgbz = getChunk(pageChunks, ChunkId.FGbz);
        FGbzChunk foregroundColors = fgbz == null ? null : new FGbzChunk(fgbz);

        Pixmap mask = getBitonalImage(sjbz, foregroundColors);
        Pixmap background = getColorImage(pageChunks.get(ChunkId.BG44));
        Pixmap foreground = getColorImage(pageChunks.get(ChunkId.FG44));

        Image image = composeImage(mask, background, foreground,
                info.getHeight(), info.getWidth(), ImageRotationType.UPSIDE_DOWN);
        if (image == null) {
            image = createBlank(info.getWidth(), info.getHeight());
        }

        return image;
    }

    private Chunk getChunk(Map<ChunkId, List<Chunk>> pageChunks, ChunkId chunkId) {
        List<Chunk> chunks = pageChunks.get(chunkId);
        if (chunks == null) {
            return null;
        }

        return chunks.stream().findFirst().orElse(null);
    }

    @Override
    public void saveChunkData(File file, long chunkId) {
        Chunk chunk = this.djvuFile.getChunkById(chunkId);

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(chunk.getData());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ChunkInfo getChunkInfo(long chunkId) {
        Chunk chunk = this.djvuFile.getChunkById(chunkId);
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
            case ChunkId.DIRM -> this.djvuFile.getDirectoryChunk();
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

    @Override
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
        buffer.append("    Composite chunks").append(NL);
        buffer.append("---------------------------------").append(NL);
        for (Map.Entry<String, Long> entry : compositeChunksStat.entrySet()) {
            buffer.append(" ")
                  .append(padRight(entry.getKey(), 15))
                  .append(": ").append(entry.getValue()).append(NL);
        }
        buffer.append(NL).append(NL);
        buffer.append("    Data chunks").append(NL);
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
        List<Chunk> chunks = this.djvuFile.getAllPageChunksWithSameChunkId(chunk);
        List<byte[]> data = chunks.stream().map(Chunk::getData).toList();
        IW44Image image = decodeIW44Image(data);

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
        Pixmap bitmap = getBitonalImage(chunk, null);

        return new ChunkInfo(chunk.getId())
            .setTextData(chunk.getDataAsText())
            .setBitmap(bitmap);
    }

    private Pixmap getBitonalImage(Chunk bitonalMask, FGbzChunk foregroundColors) {
        if (bitonalMask == null) {
            return null;
        }

        Chunk sharedShape = this.djvuFile.findSharedShapeChunk(bitonalMask);

        byte[] data = bitonalMask.getData();
        byte[] dict = sharedShape == null ? null : sharedShape.getData();

        JB2Image image = decodeJB2Image(data, dict);

        return foregroundColors == null ? image.get_bitmap() : image.get_bitmap(foregroundColors);
    }

    private Pixmap getColorImage(List<Chunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return null;
        }

        List<byte[]> data = chunks.stream().map(Chunk::getData).toList();
        IW44Image image = decodeIW44Image(data);

        return image.get_pixmap();
    }
}
