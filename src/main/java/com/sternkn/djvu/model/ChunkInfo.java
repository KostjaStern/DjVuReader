package com.sternkn.djvu.model;

import com.sternkn.djvu.file.chunks.TextZone;
import com.sternkn.djvu.file.coders.Pixmap;

import java.util.List;

public class ChunkInfo {

    private final long chunkId;
    private Pixmap bitmap;
    private String textData;

    // only for TXTz and TXTa chunks
    private List<TextZone> textZones;
    private int textZoneCount;

    public ChunkInfo(long chunkId) {
        this.chunkId = chunkId;
    }

    public long getChunkId() {
        return chunkId;
    }

    public Pixmap getBitmap() {
        return bitmap;
    }
    public ChunkInfo setBitmap(Pixmap bitmap) {
        this.bitmap = bitmap;
        return this;
    }

    public String getTextData() {
        return textData;
    }
    public ChunkInfo setTextData(String textData) {
        this.textData = textData;
        return this;
    }

    public List<TextZone> getTextZones() {
        return textZones;
    }
    public ChunkInfo setTextZones(List<TextZone> textZones) {
        this.textZones = textZones;
        return this;
    }

    public int getTextZoneCount() {
        return textZoneCount;
    }
    public ChunkInfo setTextZoneCount(int textZoneCount) {
        this.textZoneCount = textZoneCount;
        return this;
    }
}
