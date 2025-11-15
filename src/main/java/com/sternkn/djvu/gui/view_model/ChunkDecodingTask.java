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
package com.sternkn.djvu.gui.view_model;

import com.sternkn.djvu.model.DjVuModel;
import com.sternkn.djvu.model.ChunkInfo;
import javafx.concurrent.Task;

public class ChunkDecodingTask extends Task<ChunkInfo> {

    private final DjVuModel djvuModel;
    private final long chunkId;

    public ChunkDecodingTask(DjVuModel djvuModel, long chunkId) {
        this.djvuModel = djvuModel;
        this.chunkId = chunkId;
    }

    @Override
    protected ChunkInfo call() throws Exception {
        return djvuModel.getChunkInfo(chunkId);
    }
}
