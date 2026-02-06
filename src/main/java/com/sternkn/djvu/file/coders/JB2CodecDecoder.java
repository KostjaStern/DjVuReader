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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;

import static com.sternkn.djvu.utils.NumberUtils.asUnsignedShort;

public class JB2CodecDecoder {
    private static final Logger LOG = LoggerFactory.getLogger(JB2CodecDecoder.class);

    // 11.2.3 Record Types
    private static final int START_OF_DATA               = 0;
    private static final int NEW_MARK                    = 1;
    private static final int NEW_MARK_LIBRARY_ONLY       = 2;
    private static final int NEW_MARK_IMAGE_ONLY         = 3;
    private static final int MATCHED_REFINE              = 4;
    private static final int MATCHED_REFINE_LIBRARY_ONLY = 5;
    private static final int MATCHED_REFINE_IMAGE_ONLY   = 6;
    private static final int MATCHED_COPY                = 7;
    private static final int NON_MARK_DATA               = 8;
    private static final int REQUIRED_DICT_OR_RESET      = 9;
    private static final int PRESERVED_COMMENT           = 10;
    private static final int END_OF_DATA                 = 11;


    // STATIC DATA MEMBERS
    private static final int BIGPOSITIVE = 262142;
    private static final int BIGNEGATIVE = -262143;
    private static final int CELLCHUNK = 20000;
    private static final int CELLEXTRA =   500;


    private final ZPCodecDecoder zpDecoder;

    private boolean isStartRecord;

    private int cur_ncell;
    // Code comment
    private BitContext dist_comment_byte; // NumContext
    private BitContext dist_comment_length; // NumContext
    // Code values
    private BitContext dist_record_type; // NumContext
    private BitContext dist_match_index; // NumContext
    private BitContext dist_refinement_flag;
    private BitContext image_size_dist;

    private BitContext inherited_shape_count_dist;
    private BitContext rel_loc_x_current;
    private BitContext rel_loc_x_last;
    private BitContext rel_loc_y_current;
    private BitContext rel_loc_y_last;

    private BitContext abs_size_x;
    private BitContext abs_size_y;

    private BitContext abs_loc_x;
    private BitContext abs_loc_y;

    private BitContext rel_size_x;
    private BitContext rel_size_y;

    private BitContext offset_type_dist;

    private BitContext[] bitcells;
    private BitContext[] leftcell;
    private BitContext[] rightcell;

    private BitContext[] bitdist;
    private BitContext[] cbitdist;

    private int last_left;
    private int last_row_left;
    private int last_row_bottom;
    private int last_right;
    private int last_bottom;
    private int[] short_list; // int short_list[3];
    private int short_list_pos;

    private boolean refinementp;
    
    private BitContext[] getInitialBitContext(int size) {
        BitContext[] arr = new BitContext[size];
        for (int i = 0; i < size; i++) {
            arr[i] = new BitContext();
        }
        return arr;
    }

    public JB2CodecDecoder(InputStream inputStream) {
        this.zpDecoder = new ZpCodecInputStream(inputStream);

        this.dist_refinement_flag = new BitContext();
        this.offset_type_dist = new BitContext();

        resetCoder();

        this.bitdist = getInitialBitContext(1024);
        this.cbitdist = getInitialBitContext(2048);

        this.isStartRecord = false;
        this.refinementp = false;
    }

    public void decode(JB2Dict dict) {
        int rectype;

        do {
            rectype = codeRecord(dict);
        }
        while (rectype != END_OF_DATA);

        if (!isStartRecord) {
            throw new DjVuFileException("JB2Image.no_start");
        }
    }

    public void decode(JB2Image image) {
        int rectype;

        do {
            rectype = codeRecord(image);
        }
        while (rectype != END_OF_DATA);

        if (!isStartRecord) {
            throw new DjVuFileException("JB2Image.no_start");
        }
    }

    private int codeRecord(JB2Image image) {
        GBitmap cbm = null;
        GBitmap bm = null;
        JB2Shape shape = null;
        JB2Blit blit = null;
        int shapeno = -1;
        int match;

        int rectype = codeRecordType();

        switch (rectype) {
            case NEW_MARK:
            case NEW_MARK_LIBRARY_ONLY:
            case NEW_MARK_IMAGE_ONLY:
            case MATCHED_REFINE:
            case MATCHED_REFINE_LIBRARY_ONLY:
            case MATCHED_REFINE_IMAGE_ONLY:
            case NON_MARK_DATA:
            {
                shape = new JB2Shape();
                shape.setBits(new GBitmap());
                shape.setParent(-1);

                if (rectype == NON_MARK_DATA) {
                    shape.setParent(-2);
                }

                bm = shape.getBits();
                break;
            }
        }

        // Coding actions
        switch (rectype)
        {
            case START_OF_DATA:
            {
                code_image_size(image);
                code_eventual_lossless_refinement();
                image.initLibrary();
                break;
            }
            case NEW_MARK:
            {
                code_absolute_mark_size(bm, 4);
                code_bitmap_directly(bm);
                blit = code_relative_location(bm.getHeight(), bm.getWidth());
                break;
            }
            case NEW_MARK_LIBRARY_ONLY:
            {
                code_absolute_mark_size(bm, 4);
                code_bitmap_directly(bm);
                break;
            }
            case NEW_MARK_IMAGE_ONLY:
            {
                throw new DjVuFileException("Unsupported record type NEW_MARK_IMAGE_ONLY");
            }
            case MATCHED_REFINE:
            {
                match = code_match_index(image, shape);
                cbm = image.getShape(shape.getParent()).getBits();
                LibRect libRect = image.get_lib(match);
                code_relative_mark_size(bm,
                                    libRect.getRight() - libRect.getLeft() + 1,
                                    libRect.getTop() - libRect.getBottom() + 1, 4);
                code_bitmap_by_cross_coding(bm, cbm, libRect);
                blit = code_relative_location (bm.getHeight(), bm.getWidth());
                break;
            }
            case MATCHED_REFINE_LIBRARY_ONLY:
            {
                throw new DjVuFileException("Unsupported record type MATCHED_REFINE_LIBRARY_ONLY for Sjbz chunk");
            }
            case MATCHED_REFINE_IMAGE_ONLY:
            {
                throw new DjVuFileException("Unsupported record type MATCHED_REFINE_IMAGE_ONLY");
            }
            case MATCHED_COPY:
            {
                blit = new JB2Blit();
                match = code_match_index(image, blit);
                bm = image.getShape(blit.getShapeno()).getBits();
                LibRect libRect = image.get_lib(match);
                blit.setLeft(libRect.getLeft());
                blit.setBottom(libRect.getBottom());

                JB2Blit jblt;
                if (image.isReproduceOldBug()) {
                    jblt = code_relative_location(bm.getHeight(), bm.getWidth());
                }
                else {
                    jblt = code_relative_location(libRect.getTop() - libRect.getBottom() + 1,
                            libRect.getRight() - libRect.getLeft() + 1);
                }

                blit.setLeft(jblt.getLeft() - libRect.getLeft());
                blit.setBottom(jblt.getBottom() - libRect.getBottom());
                break;
            }
            case NON_MARK_DATA:
            {
                throw new DjVuFileException("Unsupported record type NON_MARK_DATA");
            }
            case PRESERVED_COMMENT:
            {
                codeComment(image);
                break;
            }
            case REQUIRED_DICT_OR_RESET:
            {
                codeInheritedShapeCount(image);
                break;
            }
            case END_OF_DATA:
            {
                break;
            }
            default:
            {
                throw new DjVuFileException("JB2Image.unknown_type");
            }
        }

        // Post-coding action
        // add shape to image
        switch(rectype)
        {
            case NEW_MARK:
            case NEW_MARK_LIBRARY_ONLY:
            case NEW_MARK_IMAGE_ONLY:
            case MATCHED_REFINE:
            case MATCHED_REFINE_LIBRARY_ONLY:
            case MATCHED_REFINE_IMAGE_ONLY:
            case NON_MARK_DATA:
            {
                shapeno = image.addShape(shape);
                break;
            }
        }
        // add shape to library
        switch(rectype)
        {
            case NEW_MARK:
            case NEW_MARK_LIBRARY_ONLY:
            case MATCHED_REFINE:
            case MATCHED_REFINE_LIBRARY_ONLY:
                image.add_library(shapeno, shape);
                break;
        }
        // make sure everything is compacted
        // decompaction will occur automatically on cross-coding bitmaps
        if (bm != null) {
            bm.compress();
        }
        // add blit to image
        switch (rectype)
        {
            case NEW_MARK:
            case NEW_MARK_IMAGE_ONLY:
            case MATCHED_REFINE:
            case MATCHED_REFINE_IMAGE_ONLY:
            case NON_MARK_DATA:
                blit.setShapeno(shapeno);
            case MATCHED_COPY:
                image.add_blit(blit);
                break;
        }

        return rectype;
    }

    private JB2Blit code_relative_location(int rows, int columns) {
        // Check start record
        if (!this.isStartRecord) {
            throw new DjVuFileException("JB2Image.no_start");
        }

        // Find location
        int bottom = 0;
        int left = 0;
        int right = 0;
        int x_diff;
        int y_diff;

        // Code offset type
        boolean new_row = codeBit(offset_type_dist);
        if (new_row)
        {
            // Begin a new row
            x_diff = get_diff(rel_loc_x_last);
            y_diff = get_diff(rel_loc_y_last);

            left = last_row_left + x_diff;
            int top = last_row_bottom + y_diff;
            right = left + columns - 1;
            bottom = top - rows + 1;

            last_left = left;
            last_row_left = left;
            last_right = right;
            last_bottom = last_row_bottom = bottom;
            fill_short_list(bottom);
        }
        else
        {
            // Same row
            x_diff = get_diff(rel_loc_x_current);
            y_diff = get_diff(rel_loc_y_current);

            left = last_right + x_diff;
            bottom = last_bottom + y_diff;
            right = left + columns - 1;

            last_left = left;
            last_right = right;
            last_bottom = update_short_list(bottom);
        }

        // Store in blit record
        JB2Blit blit = new JB2Blit();
        blit.setBottom(bottom - 1);
        blit.setLeft(left - 1);
        return blit;
    }

    private int update_short_list(int v) {
        ++short_list_pos;
        if (short_list_pos == 3) {
            short_list_pos = 0;
        }

        short_list[short_list_pos] = v;

        return (short_list[0] >= short_list[1])
            ? ((short_list[0] > short_list[2]) ? Math.max(short_list[1], short_list[2]) : short_list[0])
            : ((short_list[0] < short_list[2]) ? Math.min(short_list[1], short_list[2]) : short_list[0]);
    }

    private int get_diff(BitContext rel_loc) {
        return codeNumber(BIGNEGATIVE, BIGPOSITIVE, rel_loc, 0);
    }

    private void codeInheritedShapeCount(Dict dict) {
        if (isStartRecord) {
            resetCoder();
            return;
        }

        final int size = codeNumber(0, BIGPOSITIVE, inherited_shape_count_dist, 0);
        if (size <= 0) {
            return;
        }

        final JB2Dict inheritedDictionary = dict.getInheritedDictionary();
        if (inheritedDictionary == null) {
            throw new DjVuFileException("JB2Image.need_dict");
        }

        if (size != inheritedDictionary.getShapeCount()) {
            throw new DjVuFileException("JB2Image.bad_dict");
        }
    }

    private void resetCoder() {
        this.dist_comment_byte  = new BitContext();
        this.dist_comment_length = new BitContext();
        this.dist_record_type = new BitContext();
        this.dist_match_index = new BitContext();

        this.abs_loc_x = new BitContext();
        this.abs_loc_y = new BitContext();
        this.abs_size_x = new BitContext();
        this.abs_size_y = new BitContext();

        this.image_size_dist = new BitContext();
        this.inherited_shape_count_dist = new BitContext();

        this.rel_loc_x_current = new BitContext();
        this.rel_loc_x_last = new BitContext();
        this.rel_loc_y_current = new BitContext();
        this.rel_loc_y_last = new BitContext();
        this.rel_size_x = new BitContext();
        this.rel_size_y = new BitContext();

        this.bitcells = getInitialBitContext(CELLCHUNK + CELLEXTRA);
        this.leftcell = getInitialBitContext(CELLCHUNK + CELLEXTRA);
        this.rightcell = getInitialBitContext(CELLCHUNK + CELLEXTRA);

        this.cur_ncell = 1;
    }

    private int codeRecord(JB2Dict dict) {
        GBitmap cbm = null;
        GBitmap bm = null;
        JB2Shape shape = null;
        int shapeno = -1;
        int rectype = codeRecordType();

        switch (rectype) {
            case NEW_MARK_LIBRARY_ONLY:
            case MATCHED_REFINE_LIBRARY_ONLY:
            {
                shape = new JB2Shape();
                shape.setBits(new GBitmap());
                shape.setParent(-1);
                bm = shape.getBits();
                break;
            }
        }

        // Coding actions
        switch (rectype)
        {
            case START_OF_DATA:
            {
                if (dict == null) {
                    throw new DjVuFileException("JB2Image.bad_number");
                }

                code_image_size();
                code_eventual_lossless_refinement();
                dict.initLibrary();
                break;
            }
            case NEW_MARK_LIBRARY_ONLY:
            {
                code_absolute_mark_size(bm, 4);
                code_bitmap_directly(bm);
                break;
            }
            case MATCHED_REFINE_LIBRARY_ONLY:
            {
                int match = code_match_index(dict, shape);
                cbm = dict.getShape(shape.getParent()).getBits();
                LibRect libRect = dict.get_lib(match);
                code_relative_mark_size(bm,
                    libRect.getRight() - libRect.getLeft() + 1,
                    libRect.getTop() - libRect.getBottom() + 1, 4);

                libRect = dict.get_lib(shape.getParent());
                code_bitmap_by_cross_coding(bm, cbm, libRect);
                break;
            }
            case PRESERVED_COMMENT:
            {
                codeComment(dict);
                break;
            }
            case REQUIRED_DICT_OR_RESET:
            {
                codeInheritedShapeCount(dict);
                break;
            }
            case END_OF_DATA:
            {
                break;
            }
            default:
            {
                throw new DjVuFileException("Unsupported record type: " + rectype);
            }
        }

        // Post-coding action
        switch(rectype)
        {
            case NEW_MARK_LIBRARY_ONLY:
            case MATCHED_REFINE_LIBRARY_ONLY:
            {
                if(dict == null) {
                    throw new DjVuFileException("JB2Image.bad_number");
                }
                shapeno = dict.addShape(shape);
                dict.add_library(shapeno, shape);
                break;
            }
        }

        // make sure everything is compacted
        // decompaction will occur automatically when needed
        if (bm != null) {
            bm.compress();
        }

        return rectype;
    }

    private void code_bitmap_by_cross_coding(GBitmap bm, GBitmap cbm, LibRect libRect) {
        // Center bitmaps
        final int cw = cbm.getWidth();
        final int dw = bm.getWidth();
        final int dh = bm.getHeight();

        final int xd2c = (dw/2 - dw + 1) - ((libRect.getRight() - libRect.getLeft() + 1)/2 - libRect.getRight());
        final int yd2c = (dh/2 - dh + 1) - ((libRect.getTop() - libRect.getBottom() + 1)/2 - libRect.getTop());

        // Ensure borders are adequate
        bm.minborder(2);
        cbm.minborder(2 - xd2c);
        cbm.minborder(2 + dw + xd2c - cw);

        // Initialize row pointers
        final int dy = dh - 1;
        final int cy = dy + yd2c;

        bm.check_border();
        cbm.check_border();

        code_bitmap_by_cross_coding(bm, cbm, xd2c, dw, dy, cy);
    }

    private void code_bitmap_by_cross_coding(GBitmap bm, GBitmap cbm, int xd2c, int dw, int dy, int cy) {
        BufferPointer up1 = bm.getRow(dy + 1);
        BufferPointer up0 = bm.getRow(dy);
        BufferPointer xup1 = cbm.getRow(cy + 1).shiftPointer(xd2c);
        BufferPointer xup0 = cbm.getRow(cy).shiftPointer(xd2c);
        BufferPointer xdn1 = cbm.getRow(cy - 1).shiftPointer(xd2c);

        while (dy >= 0)
        {
            int context = get_cross_context(
                    up1, up0, xup1, xup0, xdn1, 0);
            for(int dx=0; dx < dw;)
            {
                final int n = zpDecoder.decoder(cbitdist[context]);
                up0.setValue(dx, n);
                dx++;
                context = shift_cross_context(context, n, up1, up0, xup1, xup0, xdn1, dx);
            }

            --dy; // next row
            up1 = up0;
            up0 = bm.getRow(dy);
            xup1 = xup0;
            xup0 = xdn1;
            --cy;
            xdn1 = cbm.getRow(cy - 1).shiftPointer(xd2c);
            bm.check_border();
        }
    }

    private int get_cross_context(BufferPointer up1, BufferPointer up0, BufferPointer xup1,
                                  BufferPointer xup0, BufferPointer xdn1, int column) {
        return (up1.getValue(column - 1) << 10)  |
               (up1.getValue(column) <<  9)             |
               (up1.getValue(column + 1) <<  8)  |
               (up0.getValue(column - 1) <<  7)  |
               (xup1.getValue(column) <<  6)            |
               (xup0.getValue(column - 1) <<  5) |
               (xup0.getValue(column) <<  4)            |
               (xup0.getValue(column + 1) <<  3) |
               (xdn1.getValue(column - 1) <<  2) |
               (xdn1.getValue(column) <<  1)            |
               xdn1.getValue(column + 1);
    }

    private int shift_cross_context(int context, int n, BufferPointer up1, BufferPointer up0, BufferPointer xup1,
                                    BufferPointer xup0, BufferPointer xdn1, int column) {
        return ((context << 1) & 0x636)               |
               (up1.getValue(column + 1) << 8)  |
               (xup1.getValue(column) << 6)            |
               (xup0.getValue(column + 1) << 3) |
               xdn1.getValue(column + 1)        |
               (n << 7);
    }

    private void codeComment(Dict dict) {
        if(dict == null) {
            throw new DjVuFileException("JB2Image.bad_number");
        }

        int size = codeNumber(0, BIGPOSITIVE, dist_comment_length, 0);
        char[] buffer = new char[size];

        for (int ind = 0; ind < size; ind++) {
            int ch = codeNumber(0, 255, dist_comment_byte, 0);
            buffer[ind] = (char) ch;
        }

        String comment = new String(buffer);
        dict.setComment(comment);
    }

    private void code_relative_mark_size(GBitmap bm, int cw, int ch, int border) {
        int xdiff = codeNumber(BIGNEGATIVE, BIGPOSITIVE, rel_size_x, 0);
        int ydiff = codeNumber(BIGNEGATIVE, BIGPOSITIVE, rel_size_y, 0);
        int xsize = cw + xdiff;
        int ysize = ch + ydiff;
        if ((xsize != asUnsignedShort(xsize)) || (ysize != asUnsignedShort(ysize))) {
            throw new DjVuFileException("JB2Image.bad_number");
        }

        bm.init(ysize, xsize, border);
    }

    private int code_match_index(JB2Dict dict, Parent parent) {
        List<Integer> lib2shape = dict.getLib2shape();
        int match = codeNumber(0, lib2shape.size() - 1, dist_match_index, 0);
        parent.setParent(lib2shape.get(match));
        return match;
    }

    private void code_eventual_lossless_refinement() {
        this.refinementp = codeBit(dist_refinement_flag);
    }

    private void code_image_size() {
        int w = codeNumber(0, BIGPOSITIVE, image_size_dist, 0);
        int h = codeNumber(0, BIGPOSITIVE, image_size_dist, 0);

        if (w != 0 || h != 0) {
            throw new DjVuFileException("JB2Image.bad_dict2");
        }

        this.last_left = 1;
        this.last_row_left = 0;
        this.last_row_bottom = 0;
        this.last_right = 0;
        this.isStartRecord = true;
        fill_short_list(this.last_row_bottom);
    }

    private void code_image_size(JB2Image image) {
        final int image_columns = codeNumber(0, BIGPOSITIVE, image_size_dist, 0);
        final int image_rows = codeNumber(0, BIGPOSITIVE, image_size_dist, 0);

        if (image_columns == 0 || image_rows == 0) {
            throw new DjVuFileException("JB2Image.zero_dim");
        }

        image.set_dimension(image_columns, image_rows);

        this.last_left = 1 + image_columns;
        this.last_row_left = 0;
        this.last_row_bottom = image_rows;
        this.last_right = 0;
        this.isStartRecord = true;
        fill_short_list(this.last_row_bottom);
    }

    private void fill_short_list(int v) {
        this.short_list = new int[3];
        this.short_list[0] = v;
        this.short_list[1] = v;
        this.short_list[2] = v;
        this.short_list_pos = 0;
    }

    private void code_absolute_mark_size(GBitmap bm, int border) {
        int xsize = codeNumber(0, BIGPOSITIVE, abs_size_x, 0);
        int ysize = codeNumber(0, BIGPOSITIVE, abs_size_y, 0);

        if ((xsize != asUnsignedShort(xsize)) || (ysize != asUnsignedShort(ysize))) {
            throw new DjVuFileException("JB2Image.bad_number");
        }

        bm.init(ysize, xsize, border);
    }

    private void code_bitmap_directly(GBitmap bm) {
        int dw = bm.getWidth();
        int dy = bm.getHeight() - 1;

        BufferPointer up2 = bm.getRow(dy + 2);
        BufferPointer up1 = bm.getRow(dy + 1);
        BufferPointer up0 = bm.getRow(dy);

        while (dy >= 0)
        {
            int context = get_direct_context(up2, up1, up0, 0);
            for(int dx=0; dx < dw;)
            {
                int n = zpDecoder.decoder(bitdist[context]);
                up0.setValue(dx, n);
                dx++;
                context = shift_direct_context(context, n, up2, up1, up0, dx);
            }
            // next row
            dy -= 1;
            up2 = up1;
            up1 = up0;
            up0 = bm.getRow(dy);
        }

        bm.check_border();
    }

    public int get_direct_context(BufferPointer up2, BufferPointer up1, BufferPointer up0, int column) {
        return (up2.getValue(column - 1) << 9) |
               (up2.getValue(column    ) << 8)        |
               (up2.getValue(column + 1) << 7) |
               (up1.getValue(column - 2) << 6) |
               (up1.getValue(column - 1) << 5) |
               (up1.getValue(column    ) << 4)        |
               (up1.getValue(column + 1) << 3) |
               (up1.getValue(column + 2) << 2) |
               (up0.getValue(column - 2) << 1) |
               up0.getValue(column - 1);
    }

    public int shift_direct_context(int context, int next, BufferPointer up2, BufferPointer up1, BufferPointer up0, int column) {
        return ((context << 1) & 0x37a)               |
               (up1.getValue(column + 2) << 2) |
               (up2.getValue(column + 1) << 7) |
               next;
    }

    private int codeRecordType() {
         return codeNumber(START_OF_DATA, END_OF_DATA, dist_record_type, 0);
    }

    // int JB2Dict::JB2Codec::CodeNum(int low, int high, NumContext *pctx, int v)
    private int codeNumber(int low, int high, BitContext pctx, int v) {
        if (pctx == null || pctx.getValue() >= cur_ncell) {
            throw new DjVuFileException("JB2Image.bad_numcontext");
        }

        // Start all phases
        boolean negative = false;
        int cutoff = 0;
        int phase = 1;
        int range = 0xffffffff;

        while(range != 1) {
            if (pctx.getValue() == 0) {
                final int max_ncell = bitcells.length;
                if (cur_ncell >= max_ncell) {
                    LOG.warn(" cur_ncell ({}) is greater than max_ncell ({})", cur_ncell, max_ncell);
//                    final int nmax_ncell = max_ncell + CELLCHUNK;
//                    gbitcells.resize(nmax_ncell);
//                    gleftcell.resize(nmax_ncell);
//                    grightcell.resize(nmax_ncell);
                }

                pctx.setValue(cur_ncell);
                cur_ncell ++;
                bitcells[pctx.getValue()].setValue(0);
                leftcell[pctx.getValue()].setValue(0);
                rightcell[pctx.getValue()].setValue(0);
            }

            final boolean decision = (low >= cutoff) || ((high >= cutoff) && codeBit(bitcells[pctx.getValue()]));
            pctx = decision ? rightcell[pctx.getValue()] : leftcell[pctx.getValue()];

            switch (phase)
            {
                case 1:
                    negative = !decision;
                    if (negative) {
                        final int temp = - low - 1;
                        low = - high - 1;
                        high = temp;
                    }
                    phase = 2;
                    cutoff = 1;
                    break;

                case 2:
                    if (!decision) {
                        phase = 3;
                        range = (cutoff + 1) / 2;
                        if (range == 1) {
                            cutoff = 0;
                        }
                        else {
                            cutoff -= range / 2;
                        }
                    }
                    else {
                        cutoff += cutoff + 1;
                    }
                    break;

                case 3:
                    range /= 2;
                    if (range != 1) {
                        if (!decision) {
                            cutoff -= range / 2;
                        }
                        else {
                            cutoff += range / 2;
                        }
                    }
                    else if (!decision) {
                        cutoff --;
                    }
                    break;
            }
        }
        return negative ? (- cutoff - 1) : cutoff;
    }

    private boolean codeBit(BitContext ctx) {
        return zpDecoder.decoder(ctx) != 0;
    }
}
