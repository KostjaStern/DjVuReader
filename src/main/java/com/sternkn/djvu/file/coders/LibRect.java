package com.sternkn.djvu.file.coders;

public class LibRect {

    private int top;
    private int left;
    private int right;
    private int bottom;

    public LibRect() {

    }

    public void compute_bounding_box(GBitmap bm) {
        // Avoid trouble
        // GMonitorLock lock(bm.monitor());
        // Get size
        final int w = bm.getWidth();
        final int h = bm.getHeight();
        final int s = bm.rowsize();

        // Right border
        for(right = w - 1; right >= 0; --right) {
            BufferPointer p = bm.getRow(0).shiftPointer(right);
            BufferPointer pe = p.shiftPointer(s * h);

            while(p.isPointerLess(pe) && p.isCurrentValueZero()) {
                p = p.shiftPointer(s);
            }

            if (p.isPointerLess(pe)) break;
        }

        // Top border
        for(top = h - 1; top >= 0; --top) {
            BufferPointer p = bm.getRow(top);
            BufferPointer pe = p.shiftPointer(w);

            while(p.isPointerLess(pe) && p.isCurrentValueZero()) {
                p = p.shiftPointer(1);
            }

            if (p.isPointerLess(pe)) break;
        }

        // Left border
        for (left = 0; left <= right; ++left) {
            BufferPointer p = bm.getRow(0).shiftPointer(left);
            BufferPointer pe = p.shiftPointer(s * h);

            while(p.isPointerLess(pe) && p.isCurrentValueZero()) {
                p = p.shiftPointer(s);
            }

            if (p.isPointerLess(pe)) break;
        }

        // Bottom border
        for(bottom = 0; bottom <= top; ++bottom) {
            BufferPointer p = bm.getRow(bottom);
            BufferPointer pe = p.shiftPointer(w);

            while(p.isPointerLess(pe) && p.isCurrentValueZero()) {
                p = p.shiftPointer(1);
            }

            if (p.isPointerLess(pe)) break;
        }
    }

    public int getTop() {
        return top;
    }
    public void setTop(int top) {
        this.top = top;
    }

    public int getLeft() {
        return left;
    }
    public void setLeft(int left) {
        this.left = left;
    }

    public int getRight() {
        return right;
    }
    public void setRight(int right) {
        this.right = right;
    }

    public int getBottom() {
        return bottom;
    }
    public void setBottom(int bottom) {
        this.bottom = bottom;
    }
}
