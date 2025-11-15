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
package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.coders.TestSupport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestFGbzChunk extends TestSupport {

    @Test
    public void testForegroundColorJB2ChunkDecoding() {
        Chunk chunk = readChunk("FGbz_1749.data", ChunkId.FGbz);

        FGbzChunk fgbzChunk = new FGbzChunk(chunk);

        assertEquals(0, fgbzChunk.getVersion());
        assertEquals(1, fgbzChunk.getPaletteSize());
        assertEquals(List.of(Color.BLACK), fgbzChunk.getColors());

        assertEquals(2159, fgbzChunk.getDataSize());
        assertEquals(2159, fgbzChunk.getIndexes().size());

        fgbzChunk.getIndexes().forEach(index -> {
            assertEquals(0, index);
        });
    }
}
