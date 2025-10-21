package com.sternkn.djvu.gui.view_model;

import com.sternkn.djvu.file.chunks.TextZone;
import com.sternkn.djvu.file.coders.PixelColor;
import com.sternkn.djvu.file.coders.Pixmap;
import com.sternkn.djvu.model.ChunkInfo;
import com.sternkn.djvu.model.DjVuModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ChunkDecodingWorker extends SwingWorker<ChunkInfo, Void> {
    private static final Logger LOG = LoggerFactory.getLogger(ChunkDecodingWorker.class);

    private final MainViewModel viewModel;
    private final DjVuModel djvuModel;
    private final long chunkId;

    public ChunkDecodingWorker(MainViewModel viewModel, DjVuModel djvuModel, long chunkId) {
        this.viewModel = viewModel;
        this.djvuModel = djvuModel;
        this.chunkId = chunkId;
    }

    @Override
    public ChunkInfo doInBackground() {
        return djvuModel != null ? djvuModel.getChunkInfo(chunkId) : null;
    }

    @Override
    public void done() {
        viewModel.setBusy(false);
        try {
            ChunkInfo chunkInfo = get();
            if (chunkInfo == null) {
                return;
            }

            DefaultTreeModel textTreeModel = getTextTreeModel(chunkInfo);
            viewModel.setTextTreeModel(textTreeModel);
            viewModel.setTopText(chunkInfo.getTextData());

            BufferedImage image = getImage(chunkInfo.getBitmap());
            viewModel.setImage(image);
        }
        catch (InterruptedException | ExecutionException exception) {
            LOG.error("File reading error - {}", exception.getMessage());
            viewModel.setErrorMessage(exception.getMessage());
        }
    }

    private DefaultTreeModel getTextTreeModel(ChunkInfo chunkInfo) {
        List<TextZone> textZones  = chunkInfo.getTextZones();
        if (textZones == null) {
            return null;
        }

        List<DefaultMutableTreeNode> nodes = new ArrayList<>(chunkInfo.getTextZoneCount());

        for (TextZone zone : textZones) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TextZoneNode(zone));
            nodes.add(node);
            addTextZoneChildren(node, zone.getChildren());
        }

        return new DefaultTreeModel(nodes.getFirst(), false);
    }

    private void addTextZoneChildren(DefaultMutableTreeNode parent, List<TextZone> textZones) {
        for (TextZone textZone : textZones) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TextZoneNode(textZone));
            parent.add(node);
            addTextZoneChildren(node, textZone.getChildren());
        }
    }

    private BufferedImage getImage(Pixmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        LOG.debug("bitmap: border = {}, height = {}, width = {}", bitmap.getBorder(), height,  width);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        WritableRaster raster = image.getRaster();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                PixelColor pixel = bitmap.getPixel(x, y);
                raster.setPixel(x, height - y - 1, pixel.getColor());
            }
        }

        return image;
    }
}
