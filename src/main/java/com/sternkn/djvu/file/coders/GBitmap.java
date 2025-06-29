package com.sternkn.djvu.file.coders;

import com.sternkn.djvu.file.DjVuFileException;

//import java.util.ArrayList;
//import java.util.List;

import static com.sternkn.djvu.file.utils.NumberUtils.asUnsignedShort;

public class GBitmap {

    private static final int[] ZEROBUFFER = new int[4096];// static_zerobuffer

    private static final int MAXRUNSIZE = 0x3fff;
    private static final int RUNOVERFLOWVALUE = 0xc0;
    private static final int RUNMSBMASK = 0x3f;
    private static final int RUNLSBMASK = 0xff;

    private int rows; // unsigned short nrows
    private int columns; // unsigned short ncolumns;
    private int border; // unsigned short border;
    private int bytes_per_row; // unsigned short bytes_per_row;
    private int grays; // unsigned short grays;
    // private int bytes; // unsigned char  *bytes;

    // unsigned char  *bytes_data;
    // GPBuffer<unsigned char> gbytes_data;
    private int[] bytes_data;

    // unsigned char  *rle;
    // GPBuffer<unsigned char> grle;
    // private List<Integer> rle;

    // unsigned char  **rlerows;
    // GPBuffer<unsigned char *> grlerows;
    // private List<Integer> rlerows;

    // private long rlelength; // unsigned int   rlelength;

    private int[] zero_buffer;
    // private ZeroBuffer zeroBuffer;

    public GBitmap() {
        this.rows = 0;
        this.columns = 0;
        this.border = 0;
        this.bytes_per_row = 0;
        this.grays = 0;
        this.bytes_data = null;
        // this.bytes = 0;
        // this.bytes_data = new ArrayList<>();
        // this.rle = new ArrayList<>();
        // this.rlerows = new ArrayList<>();
        // this.rlelength = 0;
    }

    public GBitmap(GBitmap ref, int border) {
        this();
        init(ref, border);
    }

    // inline unsigned int GBitmap::columns() const
    public int columns() {
        return columns;
    }

    // inline unsigned int GBitmap::rows() const
    public int rows() {
        return rows;
    }

    public int rowsize() {
        return bytes_per_row;
    }

    // inline const unsigned char * GBitmap::operator[](int row) const
    /*
    public int get(int row) {
        if (bytes == 0) {
            // ((GBitmap*)this)->uncompress();
            this.uncompress();
        }

        if (row < 0 || row >= nrows || bytes == 0) {
            if (zerosize < bytes_per_row + border)
                G_THROW( ERR_MSG("GBitmap.zero_small") );

            return zerobuffer + border;
        }
        return &bytes[row * bytes_per_row + border];
    }
     */
    public BufferPointer getRow(int row) {
        if (row < 0 || row >= rows) {
            if (zero_buffer.length < bytes_per_row + border) {
                throw new DjVuFileException("GBitmap.zero_small");
            }

            return new BufferPointer(this.zero_buffer, border);
        }

        return new BufferPointer(this.bytes_data, row * bytes_per_row + border);
    }


/*
    // void GBitmap::uncompress()
    public void uncompress() {
        // GMonitorLock lock(monitor());
        if (bytes == 0 && rle != null) {
            decode(rle);
        }
    }

    // void GBitmap::decode(unsigned char *runs)
    public void decode(List<Integer> runs) {
        // initialize pixel array
        if (nrows == 0 || ncolumns == 0) {
            // G_THROW( ERR_MSG("GBitmap.not_init") );
            throw new DjVuFileException("GBitmap.not_init");
        }

        bytes_per_row = ncolumns + border;
        if (runs == null) {
            // G_THROW( ERR_MSG("GBitmap.null_arg") );
            throw new DjVuFileException("GBitmap.null_arg");
        }

        int npixels = nrows * bytes_per_row + border;
        if (bytes_data == null)
        {
            gbytes_data.resize(npixels);
            bytes = bytes_data;
        }
        gbytes_data.clear();
        gzerobuffer=zeroes(bytes_per_row + border);
        // interpret runs data
        int c, n;
        unsigned char p = 0;
        unsigned char *row = bytes_data + border;
        n = nrows - 1;
        row += n * bytes_per_row;
        c = 0;
        while (n >= 0)
        {
            int x = read_run(runs);
            if (c+x > ncolumns)
                G_THROW( ERR_MSG("GBitmap.lost_sync2") );
            while (x-- > 0)
                row[c++] = p;
            p = 1 - p;
            if (c >= ncolumns)
            {
                c = 0;
                p = 0;
                row -= bytes_per_row;
                n -= 1;
            }
        }
        // Free rle data possibly attached to this bitmap
        grle.resize(0);
        grlerows.resize(0);
        rlelength = 0;
#ifndef NDEBUG
        check_border();
#endif
    }


    inline int GBitmap::read_run(const unsigned char *&data)
    {
        int z = *data++;
        return (z >= RUNOVERFLOWVALUE) ? ((z & ~RUNOVERFLOWVALUE) << 8) | (*data++) : z;
    }
*/
    // void GBitmap::check_border() const
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


    /** Initializes this GBitmap with the contents of the GBitmap #ref#. The
        argument #border# specifies the size of the optional border of
        white pixels surrounding the image. */
    public void init(GBitmap ref, int aborder) {
        // GMonitorLock lock(monitor());
        if (this != ref) {
            // GMonitorLock lock(ref.monitor());
            init(ref.rows, ref.columns, aborder);
            grays = ref.grays;

//            it's unclear how to copy / move data in bytes_data !!!
//            unsigned char *row = bytes_data+border;
//            for (int n = 0; n < nrows; n++, row += bytes_per_row)
//                memcpy( (void*)row, (void*)ref[n],  ncolumns );
        }
        else if (aborder > border)
        {
            minborder(aborder);
        }
    }

    /** Resets this GBitmap size to #nrows# rows and #ncolumns# columns and sets
     all pixels to white.  The argument #border# specifies the size
     of the border of white pixels surrounding the image.  The
     number of gray levels is initialized to #2#. */
    // void GBitmap::init(int arows, int acolumns, int aborder)
    // arows = 41 , acolumns = 34 , aborder = 4
    public void init(int arows, int acolumns, int aborder) {
        final int total_size = arows * (acolumns + aborder) + aborder; // np = 1562

        if (arows != asUnsignedShort(arows) ||
            acolumns != asUnsignedShort(acolumns) ||
            acolumns + aborder != asUnsignedShort(acolumns + aborder) ||
            (arows > 0 && (total_size - aborder)/arows != (acolumns + aborder)) ) {
            // G_THROW("GBitmap: image size exceeds maximum (corrupted file?)");
            throw new DjVuFileException("GBitmap: image size exceeds maximum (corrupted file?)");
        }

        // GMonitorLock lock(monitor());
        destroy();
        grays = 2;
        rows = arows;
        columns = acolumns;
        border = aborder;
        bytes_per_row = columns + border;

        this.bytes_data = new int[total_size];

        // int npixels = rows * bytes_per_row + border;
        // gzerobuffer = zeroes(bytes_per_row + border);
        this.zero_buffer = new int[bytes_per_row + border]; // new ZeroBuffer(bytes_per_row + border);
//        if (npixels > 0) {
//             bytes_data.resize(npixels);
//             bytes_data.clear();
//             bytes = bytes_data;
//        }
    }

    /*
    void
GBitmap::compress()
{
  if (grays > 2)
    G_THROW( ERR_MSG("GBitmap.cant_compress") );
  GMonitorLock lock(monitor());
  if (bytes)
    {
      grle.resize(0);
      grlerows.resize(0);
      rlelength = encode(rle,grle);
      if (rlelength)
        {
          gbytes_data.resize(0);
          bytes = 0;
        }
    }
}
     */
    public void compress() {

    }

    private void destroy() {
        this.bytes_data = null; // new ArrayList<>();
        // this.rle = new ArrayList<>();
        // this.rlerows = new ArrayList<>();
        // this.rlelength = 0;
        // this.bytes = 0;
    }

    // void GBitmap::minborder(int minimum)
    public void minborder(int minimum) {
        if (border < minimum) {
            // GMonitorLock lock(monitor());
            // if (border < minimum) {
                if (bytes_data != null) { // ????
                    GBitmap tmp = new GBitmap(this, minimum);
                    // GBitmap tmp(*this, minimum);
                    bytes_per_row = tmp.bytes_per_row;
                    // tmp.gbytes_data.swap(gbytes_data); ???
                    // bytes = bytes_data;
                    // tmp.bytes = 0;
                }
                border = minimum;
                // gzerobuffer = zeroes(border + ncolumns + border);
            zero_buffer = new int[border + columns + border]; // new ZeroBuffer(border + columns + border);
            // }
        }
    }

//    static class ZeroBuffer {
//        List<Integer> zerobuffer; // unsigned char *zerobuffer;
//
//        public ZeroBuffer(int zerosize) { // unsigned int zerosize
//            zerobuffer = new ArrayList<>(zerosize);
//        }
//    }
}
