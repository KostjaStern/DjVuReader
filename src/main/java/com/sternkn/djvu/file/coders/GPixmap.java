package com.sternkn.djvu.file.coders;

import com.sternkn.djvu.file.DjVuFileException;

import java.util.Arrays;
import java.util.Objects;

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

    public void setPixel(int x, int y, PixelColor pixel) {
        pixels[y * columns + x] = pixel;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rows, columns, Arrays.hashCode(pixels));
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof GPixmap pixmap)) {
            return false;
        }

        return pixmap.getRows() == rows &&
                pixmap.getColumns() == columns &&
                Arrays.equals(pixmap.getPixels(), pixels);
    }

    @Override
    public String toString() {
        return String.format("GPixmap{rows: %s, columns: %s}", rows, columns);
    }
}
