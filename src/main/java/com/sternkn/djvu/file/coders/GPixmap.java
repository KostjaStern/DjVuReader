package com.sternkn.djvu.file.coders;

import com.sternkn.djvu.file.DjVuFileException;

import static com.sternkn.djvu.file.utils.NumberUtils.asUnsignedShort;

public class GPixmap {

    private final int rows;
    private final int columns;
    private final PixelColor[] pixels;

    public GPixmap(int nrows, int ncolumns) {
        this.rows = nrows;
        this.columns = ncolumns;
        int np = rows * columns;

        if (rows != asUnsignedShort(rows) ||
            columns != asUnsignedShort(columns) ||
            (rows > 0 && np/rows != columns) ) {
            throw new DjVuFileException("GPixmap: image size exceeds maximum (corrupted file?)");
        }

        this.pixels = new PixelColor[np];
    }

    public PixelColor[] getPixels() {
        return pixels;
    }

    /*
         x - 0 ... columns - 1
         y - 0 ... rows - 1
    */
    public PixelColor getPixel(int x, int y) {
        return pixels[y * columns + x];
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }
}
