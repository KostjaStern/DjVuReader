package com.sternkn.djvu.file.coders;

import com.sternkn.djvu.file.DjVuFileException;


import static com.sternkn.djvu.file.utils.NumberUtils.asUnsignedShort;

public class GBitmap {

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

    public int columns() {
        return columns;
    }

    public int rows() {
        return rows;
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
}
