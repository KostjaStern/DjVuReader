package com.sternkn.djvu.file.coders;

public class LibRect {

    private int top;
    private int left;
    private int right;
    private int bottom;

    public LibRect() {

    }
/*
    public void compute_bounding_box(GBitmap bm) {
        // Avoid trouble
        // GMonitorLock lock(bm.monitor());
        // Get size
        final int w = bm.columns();
        final int h = bm.rows();
        final int s = bm.rowsize();
        // Right border
        for(right=w-1;right >= 0;--right)
        {
            unsigned char const *p = bm[0] + right;
            unsigned char const * const pe = p+(s*h);
            for (;(p<pe)&&(!*p);p+=s)
                continue;
            if (p<pe)
                break;
        }
        // Top border
        for(top=h-1;top >= 0;--top)
        {
            unsigned char const *p = bm[top];
            unsigned char const * const pe = p+w;
            for (;(p<pe)&&(!*p); ++p)
                continue;
            if (p<pe)
                break;
        }
        // Left border
        for (left=0;left <= right;++left)
        {
            unsigned char const *p = bm[0] + left;
            unsigned char const * const pe=p+(s*h);
            for (;(p<pe)&&(!*p);p+=s)
                continue;
            if (p<pe)
                break;
        }
        // Bottom border
        for(bottom=0;bottom <= top;++bottom)
        {
            unsigned char const *p = bm[bottom];
            unsigned char const * const pe = p+w;
            for (;(p<pe)&&(!*p); ++p)
                continue;
            if (p<pe)
                break;
        }
    }
*/
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
