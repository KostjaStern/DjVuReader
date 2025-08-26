package com.sternkn.djvu.file.coders;


import com.sternkn.djvu.file.DjVuFileException;

import java.util.ArrayList;
import java.util.List;

import static com.sternkn.djvu.file.utils.NumberUtils.asUnsignedInt;

/*
    Represents all the blocks of an image
 */
public class IW44ImageMap {

    private static final int IWALLOCSIZE = 4080;

    // geometry
    int iw;
    int ih;
    private int bw;
    private int bh;
    int nb;
    private int top;
    private IW44ImageBlock[] blocks;

    private List<int[]> chain;
    // array of blocks
    // IW44Image::Block *blocks;
    // IW44Image::Alloc *chain;

    public IW44ImageMap(int w, int h) {
        this.iw = w;
        this.ih = h;

        this.bw = (w + 0x20 - 1) & ~0x1f;
        this.bh = (h + 0x20 - 1) & ~0x1f;
        this.nb = (int) (asUnsignedInt((long) bw * bh) / (32 * 32));

        blocks = new IW44ImageBlock[nb];
        for (int i = 0; i < nb; i++) {
            blocks[i] = new IW44ImageBlock();
        }

        top = IWALLOCSIZE;
        chain = new ArrayList<>();
    }

    public IW44ImageBlock getBlock(int ind) {
        if (ind < 0 || ind >= nb) {
            throw new DjVuFileException("Invalid block index: " + ind);
        }
        return blocks[ind];
    }

//    public int[][] allocp(int n)  // short ** IW44Image::Map::allocp(int n)
//    {
//        // Allocate enough room for pointers plus alignment
//        int[] p = alloc( (n+1) * sizeof(short*) / sizeof(short) );
//        // Align on pointer size
//        while ( ((size_t)p) & (sizeof(short*)-1) )
//        p += 1;
//        // Cast and return
//        return (short**)p;
//    }

    public BufferPointer alloc(int n) // short * IW44Image::Map::alloc(int n)
    {
        if (top + n > IWALLOCSIZE)
        {
            // note: everything is cleared long before we use it
            // in order to avoid the need for a memory fence.
            // chain = new IW44Image::Alloc(chain);
            chain.add(new int[IWALLOCSIZE]);
            top = 0;
        }
        // short *ans = chain->data + top;
        BufferPointer buffer = new BufferPointer(chain.getLast(), top);
        top += n;
        return buffer;
    }


//    struct IW44Image::Alloc // DJVU_CLASS
//    {
//        Alloc *next;
//        short data[IWALLOCSIZE];
//        Alloc(Alloc *n);
//    };

//    IW44Image::Alloc::Alloc(Alloc *n) : next(n)
//    {
//        // see note in IW44Image::Map::alloc
//        memset(data, 0, sizeof(data));
//    }


//    public int getTop() {
//        return top;
//    }
}
