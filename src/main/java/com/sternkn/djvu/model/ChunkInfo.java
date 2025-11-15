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
