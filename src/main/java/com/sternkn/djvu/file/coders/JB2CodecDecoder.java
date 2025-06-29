package com.sternkn.djvu.file.coders;

import com.sternkn.djvu.file.DjVuFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.sternkn.djvu.file.utils.NumberUtils.asUnsignedShort;

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

    // ??
    private char gotstartrecordp;

    private int cur_ncell;
    // Code comment
    private BitContext dist_comment_byte; // NumContext
    private BitContext dist_comment_length; // NumContext
    // Code values
    private BitContext dist_record_type; // NumContext
    private BitContext dist_match_index; // NumContext
    private BitContext dist_refinement_flag;
    private BitContext image_size_dist;

    private BitContext abs_size_x;
    private BitContext abs_size_y;

    private BitContext rel_size_x;
    private BitContext rel_size_y;

//    private BitContext bitcells;
//    private BitContext leftcell;
//    private BitContext rightcell;
    private BitContext[] bitcells;
    private BitContext[] leftcell;
    private BitContext[] rightcell;

    private BitContext[] bitdist;
    private BitContext[] cbitdist;

    private int last_left;
    private int last_row_left;
    private int last_row_bottom;
    private int last_right;
    // private int gotstartrecordp; // char
    private int[] short_list; // int short_list[3];
    private int short_list_pos;

    private boolean refinementp;

//    private List<Integer> shape2lib;
//    private List<Integer> lib2shape;
//    private List<LibRect> libinfo;


    private BitContext[] getInitialBitContext(int size) {
        BitContext[] arr = new BitContext[size];
        for (int i = 0; i < size; i++) {
            arr[i] = new BitContext();
        }
        return arr;
    }

    /*
    JB2Dict::JB2Codec::JB2Codec(const bool xencoding)
  : encoding(xencoding),
    cur_ncell(0),
    gbitcells(bitcells,CELLCHUNK+CELLEXTRA),
    gleftcell(leftcell,CELLCHUNK+CELLEXTRA),
    grightcell(rightcell,CELLCHUNK+CELLEXTRA),
    refinementp(false),
    gotstartrecordp(0),
    dist_comment_byte(0),
    dist_comment_length(0),
    dist_record_type(0),
    dist_match_index(0),
    dist_refinement_flag(0),
    abs_loc_x(0),
    abs_loc_y(0),
    abs_size_x(0),
    abs_size_y(0),
    image_size_dist(0),
    inherited_shape_count_dist(0),
    offset_type_dist(0),
    rel_loc_x_current(0),
    rel_loc_x_last(0),
    rel_loc_y_current(0),
    rel_loc_y_last(0),
    rel_size_x(0),
    rel_size_y(0)
{
  memset(bitdist, 0, sizeof(bitdist));
  memset(cbitdist, 0, sizeof(cbitdist));
  // Initialize numcoder
  bitcells[0] = 0; // dummy cell
  leftcell[0] = rightcell[0] = 0;
  cur_ncell = 1;
}
     */
    public JB2CodecDecoder(InputStream inputStream) {
        this.zpDecoder = new ZpCodecInputStream(inputStream);

        this.dist_comment_byte  = new BitContext();
        this.dist_comment_length = new BitContext();
        this.dist_record_type = new BitContext();
        this.dist_match_index = new BitContext();
        this.dist_refinement_flag = new BitContext();

        this.image_size_dist = new BitContext();
        this.abs_size_x = new BitContext();
        this.abs_size_y = new BitContext();

        this.rel_size_x = new BitContext();
        this.rel_size_y = new BitContext();

//        this.bitcells = new BitContext();
//        this.leftcell = new BitContext();
//        this.rightcell = new BitContext();
        this.bitcells = getInitialBitContext(CELLCHUNK + CELLEXTRA);
        this.leftcell = getInitialBitContext(CELLCHUNK + CELLEXTRA);
        this.rightcell = getInitialBitContext(CELLCHUNK + CELLEXTRA);

        this.bitdist = getInitialBitContext(1024);
        this.cbitdist = getInitialBitContext(2048);

        this.refinementp = false;
        this.cur_ncell = 1;
    }

    // JB2Image.cpp
    // void JB2Dict::JB2Codec::Decode::code(const GP<JB2Dict> &gjim)
    public void decode(JB2Dict dict) {
        int rectype;
        // JB2Shape tmpshape = new JB2Shape();

        do {
            rectype = codeRecord(dict);
        }
        while (rectype != END_OF_DATA);

        // return;
    }

    // void JB2Dict::JB2Codec::code_record(int &rectype, const GP<JB2Dict> &gjim, JB2Shape *xjshp)
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
//                if (shape == null) {
//                    throw new DjVuFileException("JB2Image.bad_number");
//                }
                shape = new JB2Shape();
                shape.setBits(new GBitmap()); // = GBitmap::create();
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
                    // G_THROW( ERR_MSG("JB2Image.bad_number") );
                }
                // JB2Dict &jim=*gjim;
                code_image_size(dict);
                code_eventual_lossless_refinement();
                // if (! encoding) // encoding = false
                dict.init_library();
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
//                if(shape == null || gjim == null) {
//                    throw new DjVuFileException("JB2Image.bad_number");
//                    // G_THROW( ERR_MSG("JB2Image.bad_number") );
//                }
                // JB2Dict &jim=*gjim;
                // JB2Shape &jshp=*xjshp;
                int match = code_match_index(dict, shape); // shape.getParent()
                cbm = dict.get_shape(shape.getParent()).getBits();
                LibRect libRect = dict.get_lib(match); // libinfo.get(match);
                code_relative_mark_size(bm,
                                        libRect.getRight() - libRect.getLeft() + 1,
                                        libRect.getTop() - libRect.getBottom() + 1, 4);

                libRect = dict.get_lib(shape.getParent());
                code_bitmap_by_cross_coding(bm, cbm, libRect);
                break;
            }
            case PRESERVED_COMMENT:
            {
                if(dict != null) {
                    throw new DjVuFileException("JB2Image.bad_number");
                    // G_THROW( ERR_MSG("JB2Image.bad_number") );
                }
                // JB2Dict &jim=*gjim;
                // code_comment(jim.comment);
                break;
            }
            case REQUIRED_DICT_OR_RESET:
            {
//                if (! gotstartrecordp)
//                {
//                    // Indicates need for a shape dictionary
//                    if(!gjim)
//                    {
//                        G_THROW( ERR_MSG("JB2Image.bad_number") );
//                    }
//                    code_inherited_shape_count(*gjim);
//                }else
//                    // Reset all numerical contexts to zero
//                    reset_numcoder();
                break;
            }
            case END_OF_DATA:
            {
                break;
            }
            default:
            {
                // G_THROW( ERR_MSG("JB2Image.bad_type") );
            }
        }
        // Post-coding action
        // if (!encoding) // encoding = false
        // {
            // add shape to dictionary
            switch(rectype)
            {
                case NEW_MARK_LIBRARY_ONLY:
                case MATCHED_REFINE_LIBRARY_ONLY:
                {
                    // xjshp -> shape
                    // gjim -> dict
                    if(dict == null) { // shape == null ||
                        // G_THROW( ERR_MSG("JB2Image.bad_number") );
                        throw new DjVuFileException("JB2Image.bad_number");
                    }
                    // JB2Shape &jshp=*xjshp;
                    shapeno = dict.add_shape(shape);
                    dict.add_library(shapeno, shape);
                    break;
                }
            }
            // make sure everything is compacted
            // decompaction will occur automatically when needed
            if (bm != null) {
                bm.compress();
            }
        // }


        return rectype;
    }

    // void JB2Dict::JB2Codec::code_bitmap_by_cross_coding (GBitmap &bm, GP<GBitmap> &cbm, const int libno)
    void code_bitmap_by_cross_coding(GBitmap bm, GBitmap cbm, LibRect libRect) {
        // Make sure bitmaps will not be disturbed
        // GBitmap copycbm = new GBitmap(); // GBitmap::create()
        // if (cbm.monitor())
        // {
            // Perform a copy when the bitmap is explicitely shared
            // GMonitorLock lock2(cbm->monitor());
            // copycbm.init(cbm, 0);
            // cbm = copycbm;
        // }
        // GMonitorLock lock1(bm.monitor());
        // Center bitmaps
        final int cw = cbm.columns();
        final int dw = bm.columns();
        final int dh = bm.rows();
        // final LibRect libRect = libinfo.get(libno);
        // xd2c = 0 , yd2c = 0
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
        BufferPointer up1 = bm.getRow(dy + 1); // bm[dy+1]
        BufferPointer up0 = bm.getRow(dy); // bm[dy]
        BufferPointer xup1 = cbm.getRow(cy + 1).shiftPointer(xd2c); // (*cbm)[cy+1] + xd2c
        BufferPointer xup0 = cbm.getRow(cy).shiftPointer(xd2c); // (*cbm)[cy  ] + xd2c
        BufferPointer xdn1 = cbm.getRow(cy - 1).shiftPointer(xd2c); // (*cbm)[cy-1] + xd2c

        // zpDecoder.decoder(bitdist[context]);
        // ZPCodec &zp=*gzp;
        // iterate on rows (decoding)
        while (dy >= 0)
        {
            int context = get_cross_context(
                    up1, up0, xup1, xup0, xdn1, 0);
            for(int dx=0; dx < dw;)
            {
                final int n = zpDecoder.decoder(cbitdist[context]);
                // up0[dx++] = n;
                up0.setValue(dx, n);
                dx++;
                context = shift_cross_context(context, n, up1, up0, xup1, xup0, xdn1, dx);
            }
            // next row
            --dy;
            up1 = up0;
            up0 = bm.getRow(dy);
            xup1 = xup0;
            xup0 = xdn1;
            --cy; // [(--cy)-1] + xd2c;
            xdn1 = cbm.getRow(cy - 1).shiftPointer(xd2c); // [(--cy)-1] + xd2c;
// #ifndef NDEBUG
            bm.check_border();
// #endif
        }
    }

    private int get_cross_context(BufferPointer up1, BufferPointer up0, BufferPointer xup1,
                                  BufferPointer xup0, BufferPointer xdn1, int column)
    {
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

    // void JB2Dict::JB2Codec::Decode::code_relative_mark_size(GBitmap &bm, int cw, int ch, int border)
    private void code_relative_mark_size(GBitmap bm, int cw, int ch, int border) {
        int xdiff = codeNumber(BIGNEGATIVE, BIGPOSITIVE, rel_size_x, 0);
        int ydiff = codeNumber(BIGNEGATIVE, BIGPOSITIVE, rel_size_y, 0);
        int xsize = cw + xdiff;
        int ysize = ch + ydiff;
        if ((xsize != asUnsignedShort(xsize)) || (ysize != asUnsignedShort(ysize))) {
            // G_THROW( ERR_MSG("JB2Image.bad_number") );
            throw new DjVuFileException("JB2Image.bad_number");
        }

        bm.init(ysize, xsize, border);
    }

    /*
    int JB2Dict::JB2Codec::Decode::code_match_index(int &index, JB2Dict &)
{
    // match = 3,
    int match=CodeNum(0, lib2shape.hbound(), dist_match_index); // lib2shape.hbound() = 3,
    index = lib2shape[match]; // index = 3
    return match;
}
     */
    // int JB2Dict::JB2Codec::Decode::code_match_index(int &index, JB2Dict &)
    private int code_match_index(JB2Dict dict, JB2Shape shape) {
        List<Integer> lib2shape = dict.getLib2shape();
        // lib2shape.hbound() - Returns the upper bound of the valid subscript range.
        int match = codeNumber(0, lib2shape.size() - 1, dist_match_index, 0); // lib2shape.hbound() ? size
        shape.setParent(lib2shape.get(match));
        return match;
    }

    // inline void JB2Dict::JB2Codec::code_eventual_lossless_refinement(void)  (see JB2Image.h)
    private void code_eventual_lossless_refinement() {
        this.refinementp = codeBit(dist_refinement_flag);
    }

    // codeNumber(START_OF_DATA, END_OF_DATA, dist_record_type, 0);
    // void JB2Dict::JB2Codec::Decode::code_image_size(JB2Dict &jim)
    private void code_image_size(JB2Dict dict) {
        int w = codeNumber(0, BIGPOSITIVE, image_size_dist, 0);
        int h = codeNumber(0, BIGPOSITIVE, image_size_dist, 0);

        if (w != 0 || h != 0) {
            throw new DjVuFileException("JB2Image.bad_dict2");
        }

        // JB2Codec::code_image_size(jim);
        this.last_left = 1;
        this.last_row_left = 0;
        this.last_row_bottom = 0;
        this.last_right = 0;
        this.gotstartrecordp = 1;
        fill_short_list(this.last_row_bottom);
    }

    // inline void JB2Dict::JB2Codec::fill_short_list(const int v) (see JB2Image.h)
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

    private void code_bitmap_directly(GBitmap bm)
    {
        int dw = bm.columns();
        int dy = bm.rows() - 1;

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
        return zpDecoder.decoder(ctx) != 0; // gzp->decoder(ctx)?true:false;
    }
}
