package com.sternkn.djvu.file.chunks;

public record GRect(int xmin, int ymin, int xmax, int ymax) {

    public int getWidth() {
        return xmax - xmin;
    }

    public int getHeight() {
        return ymax - ymin;
    }

    public boolean isEmpty() {
        return (xmin >= xmax) || (ymin >= ymax);
    }

    public int area() {
        return isEmpty() ? 0 : getWidth() * getHeight();
    }


}
