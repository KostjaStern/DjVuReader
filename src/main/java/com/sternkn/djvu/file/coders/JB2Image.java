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
package com.sternkn.djvu.file.coders;

import com.sternkn.djvu.file.DjVuFileException;
import com.sternkn.djvu.file.chunks.Color;
import com.sternkn.djvu.file.chunks.FGbzChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.sternkn.djvu.utils.NumberUtils.asUnsignedInt;

public class JB2Image extends JB2Dict implements Dict {
    private static final Logger LOG = LoggerFactory.getLogger(JB2Image.class);

    private int width;
    private int height;

    private final List<JB2Blit> blits;
    private final boolean reproduce_old_bug;

    public JB2Image() {
        super();

        this.blits = new ArrayList<>();
        this.reproduce_old_bug = false;
    }

    public void set_dimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public boolean isReproduceOldBug() {
        return reproduce_old_bug;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int get_blit_count() {
        return blits.size();
    }

    public JB2Blit get_blit(int index) {
        return blits.get(index);
    }

    public int add_blit(JB2Blit blit) {
        if (blit.getShapeno() >= asUnsignedInt(get_shape_count())) {
            throw new DjVuFileException("JB2Image.bad_shape");
        }

        int index = blits.size();
        blits.add(blit);
        return index;
    }

    public GBitmap get_bitmap() {
        return this.get_bitmap(1, 1, null);
    }

    public GBitmap get_bitmap(FGbzChunk foregroundColors) {
        return this.get_bitmap(1, 1, foregroundColors);
    }

    public GBitmap get_bitmap(int subsample, int align, FGbzChunk foregroundColors) {
        if (this.width == 0 || this.height == 0) {
            throw new DjVuFileException("JB2Image.cant_create");
        }

        int swidth = (width + subsample - 1) / subsample;
        int sheight = (height + subsample - 1) / subsample;
        int border = ((swidth + align - 1) & -align) - swidth;

        final int blitCount = get_blit_count();
        LOG.debug("blitCount = {}", blitCount);

        List<Color> paletteColors = null;
        if (foregroundColors != null
                && foregroundColors.getIndexes().size() == blitCount) {
            paletteColors = foregroundColors.getColors();
        }

        GBitmap bm = new GBitmap(paletteColors);
        bm.init(sheight, swidth, border);
        bm.set_grays(1 + subsample * subsample);

        for (int blitno = 0; blitno < blitCount; blitno++)
        {
           JB2Blit pblit = get_blit(blitno);
           JB2Shape  pshape = get_shape(pblit.getShapeno());
            GBitmap pshapeBits = pshape.getBits();

            if (pshapeBits != null) {
                Integer colorIndex = null;
                if (foregroundColors != null
                    && foregroundColors.getIndexes() != null
                    && foregroundColors.getIndexes().size() == blitCount) {
                    colorIndex = foregroundColors.getIndexes().get(blitno);
                }

                bm.blit(pshapeBits, pblit.getLeft(), pblit.getBottom(), subsample, colorIndex);
            }
        }
        return bm;
    }
}
