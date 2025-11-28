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
import static com.sternkn.djvu.utils.utils.NumberUtils.asUnsignedShort;

public class GBitmap implements Pixmap {

    private int rows;
    private int columns;
    private int border;
    private int bytes_per_row;
    private int grays;

    private int[] bytes_data;
    private int[] zero_buffer;

    public GBitmap() {
        this.rows = 0;
        this.columns = 0;
        this.border = 0;
        this.bytes_per_row = 0;
        this.grays = 0;
        this.bytes_data = null;
    }

    public GBitmap(GBitmap ref, int border) {
        this();
        init(ref, border);
    }

    @Override
    public int getWidth() {
        return columns;
    }

    @Override
    public int getHeight() {
        return rows;
    }

    @Override
    public int getBorder() {
        return border;
    }

    public int[] getBytesData() {
        return bytes_data;
    }

    public int rowsize() {
        return bytes_per_row;
    }

    public BufferPointer getRow(int row) {
        if (row < 0 || row >= rows) {
            if (zero_buffer.length < bytes_per_row + border) {
                throw new DjVuFileException("GBitmap.zero_small");
            }

            return new BufferPointer(this.zero_buffer, border);
        }

        return new BufferPointer(this.bytes_data, row * bytes_per_row + border);
    }

    @Override
    public PixelColor getPixel(int x, int y) {
        int index = y * bytes_per_row + border + x;
        int value = bytes_data[index];
        return value == 0 ? PixelColor.WHITE : PixelColor.BLACK;
    }

    public void check_border() {
        if (this.bytes_data == null) {
            return;
        }

        BufferPointer p = getRow(-1);
        for (int col = -border; col < columns + border; col++) {
            if (p.getValue(col) != 0) {
                throw new DjVuFileException("GBitmap.zero_damaged");
            }
        }

        for (int row = 0; row < rows; row++) {
            p = getRow(row);
            for (int col = -border; col < 0; col++) {
                if (p.getValue(col) != 0) {
                    throw new DjVuFileException("GBitmap.left_damaged");
                }
            }

            for (int col = columns; col < columns + border; col++) {
                if (p.getValue(col) != 0) {
                    throw new DjVuFileException("GBitmap.right_damaged");
                }
            }
        }
    }

    /**
     *  Initializes this GBitmap with the contents of the GBitmap {ref}. The argument {aborder} specifies
     *  the size of the optional border of white pixels surrounding the image.
     */
    public void init(GBitmap ref, int aborder) {
        if (this != ref) {
            init(ref.rows, ref.columns, aborder);
            grays = ref.grays;
        }
        else if (aborder > border)
        {
            minborder(aborder);
        }
    }

    /**
     * Resets this GBitmap size to {arows} rows and {acolumns} columns and sets all pixels to white.
     * The argument {aborder} specifies the size of the border of white pixels surrounding the image.
     * The number of gray levels is initialized to {2}.
     */
    public void init(int arows, int acolumns, int aborder) {
        final int total_size = arows * (acolumns + aborder) + aborder;

        if (arows != asUnsignedShort(arows) ||
            acolumns != asUnsignedShort(acolumns) ||
            acolumns + aborder != asUnsignedShort(acolumns + aborder) ||
            (arows > 0 && (total_size - aborder)/arows != (acolumns + aborder)) ) {
            throw new DjVuFileException("GBitmap: image size exceeds maximum (corrupted file?)");
        }

        grays = 2;
        rows = arows;
        columns = acolumns;
        border = aborder;
        bytes_per_row = columns + border;

        this.bytes_data = new int[total_size];
        this.zero_buffer = new int[bytes_per_row + border];
    }

    public void compress() {
        // TODO: implementation
    }

    public void minborder(int minimum) {
        if (border >= minimum) {
            return;
        }

        if (bytes_data != null) {
            GBitmap tmp = new GBitmap(this, minimum);
            // GBitmap tmp(*this, minimum);
            bytes_per_row = tmp.bytes_per_row;
            // tmp.gbytes_data.swap(gbytes_data); ???
            // bytes = bytes_data;
            // tmp.bytes = 0;
        }

        border = minimum;
        zero_buffer = new int[border + columns + border];
    }

    public void set_grays(int ngrays) {
        if (ngrays < 2 || ngrays > 256) {
           throw new DjVuFileException("GBitmap.bad_levels");
        }

        grays = ngrays;
//        if (ngrays > 2 && bytes_data == null) {
//            uncompress();
//        }
    }

    void blit(GBitmap bm, int x, int y) {
        // Check boundaries
        if ((x >= columns)         ||
            (y >= rows)            ||
            (x + bm.getWidth() < 0) ||
            (y + bm.getHeight() < 0) ) {
            return;
        }

        // Perform blit
        // GMonitorLock lock1(monitor());
        // GMonitorLock lock2(bm->monitor());
        if (bm.bytes_data != null)
        {
//            if (!bytes_data)
//                uncompress();
            // Blit from bitmap
            // const unsigned char *srow = bm->bytes + bm->border;
            BufferPointer srow = new BufferPointer(bm.bytes_data, bm.border);
            BufferPointer drow = new BufferPointer(bytes_data, border + y * bytes_per_row + x);
            // unsigned char *drow = bytes_data + border + y * bytes_per_row + x;
            for (int sr = 0; sr < bm.getHeight(); sr++)
            {
                if ((sr + y >= 0) && (sr + y < this.rows))
                {
                    int sc = Math.max(0, -x);
                    int sc1 = Math.min(bm.getWidth(), this.columns - x);
                    while (sc < sc1)
                    {
                        int newDrowValue = drow.getValue(sc) + srow.getValue(sc);
                        drow.setValue(sc, newDrowValue); // drow[sc] += srow[sc];
                        sc += 1;
                    }
                }

                srow = srow.shiftPointer(bm.bytes_per_row); // += bm->bytes_per_row;
                drow = drow.shiftPointer(bytes_per_row); // drow += bytes_per_row;
            }
        }
//        else if (bm.rle) // for the compressed bitmap
//        {
//            if (!bytes_data)
//                uncompress();
//            // Blit from rle
//            const unsigned char *runs = bm->rle;
//            unsigned char *drow = bytes_data + border + y*bytes_per_row + x;
//            int sr = bm->nrows - 1;
//            drow += sr * bytes_per_row;
//            int sc = 0;
//            char p = 0;
//            while (sr >= 0)
//            {
//                const int z = read_run(runs);
//                if (sc+z > bm->ncolumns)
//                    G_THROW( ERR_MSG("GBitmap.lost_sync") );
//                int nc = sc + z;
//                if (p && sr+y>=0 && sr+y<nrows)
//                {
//                    if (sc + x < 0)
//                        sc = min(-x, nc);
//                    while (sc < nc && sc + x<ncolumns)
//                        drow[sc++] += 1;
//                }
//                sc = nc;
//                p = 1 - p;
//                if (sc >= bm->ncolumns)
//                {
//                    p = 0;
//                    sc = 0;
//                    drow -= bytes_per_row;
//                    sr -= 1;
//                }
//            }
//        }
    }

    public void blit(GBitmap bm, int xh, int yh, int subsample) {
        // Use code when no subsampling is necessary
        if (subsample == 1)
        {
            blit(bm, xh, yh);
            return;
        }

        // Check boundaries
        if ((xh >= this.columns * subsample) ||
            (yh >= this.rows * subsample)    ||
            (xh + bm.getWidth() < 0)          ||
            (yh + bm.getHeight() < 0) ) {
            return;
        }

        // Perform subsampling blit
//        GMonitorLock lock1(monitor());
//        GMonitorLock lock2(bm->monitor());
        if (bm.bytes_data != null)
        {
//            if (!bytes_data)
//                uncompress();
            // Blit from bitmap
            BitContext dr = new BitContext();
            BitContext dr1 = new BitContext();
            BitContext zdc = new BitContext();
            BitContext zdc1 = new BitContext();

            euclidian_ratio(yh, subsample, dr, dr1);
            euclidian_ratio(xh, subsample, zdc, zdc1);

            BufferPointer srow = new BufferPointer(bm.bytes_data, bm.border);
            BufferPointer drow = new BufferPointer(bytes_data, border + dr.getValue() * bytes_per_row);
            for (int sr = 0; sr < bm.getHeight(); sr++)
            {
                if (dr.getValue() >= 0 && dr.getValue() < this.rows)
                {
                    BitContext dc = new BitContext(zdc.getValue());
                    BitContext dc1 = new BitContext(zdc1.getValue());
                    for (int sc = 0; sc < bm.getWidth(); sc++)
                    {
                        int dcValue = dc.getValue();
                        if (dcValue >= 0 && dcValue < this.columns) {
                            // drow[dc] += srow[sc];
                            drow.setValue(dcValue, drow.getValue(dcValue) + srow.getValue(sc));
                        }
                        dc1.setValue(dc1.getValue() + 1);
                        if (dc1.getValue() >= subsample)
                        {
                            dc1.setValue(0); // = 0;
                            dc.setValue(dc.getValue() + 1); // dc += 1;
                        }
                    }
                }
                // next line in source
                srow.shiftPointer(bm.bytes_per_row); // += bm->bytes_per_row;
                // next line fraction in destination
                dr1.setValue(dr1.getValue() + 1);
                if (dr1.getValue() >= subsample)
                {
                    dr1.setValue(0); // = 0;
                    dr.setValue(dr.getValue() + 1); // dr += 1;
                    drow.shiftPointer(bytes_per_row);
                }
            }
        }
/*
        else if (bm->rle)
        {
            if (!bytes_data)
                uncompress();
            // Blit from rle
            int dr, dr1, zdc, zdc1;
            euclidian_ratio(yh+bm->nrows-1, subsample, dr, dr1);
            euclidian_ratio(xh, subsample, zdc, zdc1);
      const unsigned char *runs = bm->rle;
            unsigned char *drow = bytes_data + border + dr*bytes_per_row;
            int sr = bm->nrows -1;
            int sc = 0;
            char p = 0;
            int dc = zdc;
            int dc1 = zdc1;
            while (sr >= 0)
            {
                int z = read_run(runs);
                if (sc+z > bm->ncolumns)
                    G_THROW( ERR_MSG("GBitmap.lost_sync") );
                int nc = sc + z;

                if (dr>=0 && dr<nrows)
                    while (z>0 && dc<ncolumns)
                    {
                        int zd = subsample - dc1;
                        if (zd > z)
                            zd = z;
                        if (p && dc>=0)
                            drow[dc] += zd;
                        z -= zd;
                        dc1 += zd;
                        if (dc1 >= subsample)
                        {
                            dc1 = 0;
                            dc += 1;
                        }
                    }
                // next fractional row
                sc = nc;
                p = 1 - p;
                if (sc >= bm->ncolumns)
                {
                    sc = 0;
                    dc = zdc;
                    dc1 = zdc1;
                    p = 0;
                    sr -= 1;
                    if (--dr1 < 0)
                    {
                        dr1 = subsample - 1;
                        dr -= 1;
                        drow -= bytes_per_row;
                    }
                }
            }

 */
    }

    private void euclidian_ratio(int a, int b, BitContext q, BitContext r) {
        q.setValue(a / b);
        r.setValue(a - b * q.getValue());
        if (r.getValue() < 0)
        {
            q.setValue(q.getValue() - 1); // -= 1;
            r.setValue(r.getValue() + b); // r += b;
        }
    }
}
