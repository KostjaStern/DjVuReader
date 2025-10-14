package com.sternkn.djvu.file.coders;

import com.sternkn.djvu.file.DjVuFileException;

public class BSort {
    // Sorting tresholds
    private static final int RANKSORT_THRESH = 10;
    private static final int RADIX_THRESH = 32768;
    private static final int PRESORT_DEPTH = 8;
    private static final int QUICKSORT_STACK = 512;
    private static final int PRESORT_THRESH = 10;

    private int[] data;
    private int[] rank;

    private int[] posn;
    private int size;

    public BSort(int[] data, int size) {
        if (size <= 0 || size >= 0x1000000) {
            throw new DjVuFileException("Invalid size: " + size);
        }

        this.data = data;
        this.size = size;

        this.posn = new int[size];
        this.rank = new int[size + 1];
        this.rank[size] = -1;
    }

    public int run() {
        int lo;
        int hi;

        if (data[size - 1] != 0) {
            throw new DjVuFileException("Invalid data state: data[size - 1] should be zero");
        }

        // Step 1: Radix sort
        int depth = 0;
        if (size > RADIX_THRESH)
        {
            radixsort16();
            depth = 2;
        }
        else
        {
            radixsort8();
            depth = 1;
        }

        // Step 2: Perform presort to depth PRESORT_DEPTH
        for (lo = 0; lo < size; lo++)
        {
            hi = rank[posn[lo]];
            if (lo < hi) {
                quicksort3d(lo, hi, depth);
            }
            lo = hi;
        }
        depth = PRESORT_DEPTH;

        // Step 3: Perform rank doubling
        int again = 1;
        while (again != 0)
        {
            again = 0;
            int sorted_lo = 0;
            for (lo = 0; lo < size; lo++)
            {
                hi = rank[posn[lo] & 0xffffff];
                if (lo == hi) {
                    lo += (posn[lo] >> 24) & 0xff;
                }
                else
                {
                    if (hi - lo < RANKSORT_THRESH) {
                        ranksort(lo, hi, depth);
                    }
                    else {
                        again += 1;
                        while (sorted_lo < lo-1) {
                            int step = Math.min(255, lo - 1 - sorted_lo);
                            posn[sorted_lo] = (posn[sorted_lo] & 0xffffff) | (step << 24);
                            sorted_lo += step + 1;
                        }
                        quicksort3r(lo, hi, depth);
                        sorted_lo = hi + 1;
                    }
                    lo = hi;
                }
            }
            // Finish threading
            while (sorted_lo < lo - 1) {
                int step = Math.min(255, lo - 1 - sorted_lo);
                posn[sorted_lo] = (posn[sorted_lo] & 0xffffff) | (step << 24);
                sorted_lo += step + 1;
            }

            // Double depth
            depth += depth;
        }

        // Step 4: Permute data
        int markerpos = -1;
        for (int i = 0; i < size; i++) {
            rank[i] = data[i];
        }

        for (int i = 0; i < size; i++) {
            int j = posn[i] & 0xffffff;
            if (j > 0) {
                data[i] = rank[j - 1];
            }
            else {
                data[i] = 0;
                markerpos = i;
            }
        }

        if (markerpos < 0) {
            throw new DjVuFileException("Invalid data state: result is out of range");
        }

        return markerpos;
    }

    // quicksort3r -- Three way quicksort algorithm
    // Sort suffixes based on rank at pos+depth
    // The algorithm breaks into ranksort when size is
    // smaller than RANKSORT_THRESH
    private void quicksort3r(int lo, int hi, int depth) {
        /* Initialize stack */
        int[] slo = new int[QUICKSORT_STACK];
        int[] shi = new int[QUICKSORT_STACK];
        int sp = 1;
        slo[0] = lo;
        shi[0] = hi;

        // Recursion elimination loop
        while (--sp >= 0)
        {
            lo = slo[sp];
            hi = shi[sp];
            // Test for insertion sort
            if (hi - lo < RANKSORT_THRESH) {
                ranksort(lo, hi, depth);
            }
            else {
                int tmp;
                BufferPointer rr = new BufferPointer(rank, depth);
                int med = pivot3r(rr, lo, hi);

                // -- positions are organized as follows:
                //   [lo..l1[ [l1..l[ ]h..h1] ]h1..hi]
                //      =        <       >        =
                int l1 = lo;
                int h1 = hi;
                while (rr.getValue(posn[l1]) == med && l1 < h1) {
                    l1++;
                }
                while (rr.getValue(posn[h1]) == med && l1 < h1) {
                    h1--;
                }
                int l = l1;
                int h = h1;

                // -- partition set
                for (;;)
                {
                    while (l <= h) {
                        int c = rr.getValue(posn[l]) - med;
                        if (c > 0) break;
                        if (c == 0) {
                            tmp = posn[l];
                            posn[l] = posn[l1];
                            posn[l1++] = tmp;
                        }
                        l++;
                    }
                    while (l <= h)
                    {
                        int c = rr.getValue(posn[h]) - med;
                        if (c < 0) break;
                        if (c == 0) {
                            tmp = posn[h];
                            posn[h] = posn[h1];
                            posn[h1--] = tmp;
                        }
                        h--;
                    }
                    if (l > h) break;
                    tmp = posn[l];
                    posn[l] = posn[h];
                    posn[h] = tmp;
                }

                // -- reorganize as follows
                //   [lo..l1[ [l1..h1] ]h1..hi]
                //      <        =        >
                tmp = Math.min(l1 - lo, l - l1);
                vswap(lo, l - tmp, tmp, posn);
                l1 = lo + (l - l1);
                tmp = Math.min(hi - h1, h1 - h);
                vswap(hi - tmp + 1, h + 1, tmp, posn);
                h1 = hi - (h1 - h);

                // -- process segments
                if (sp + 2 >= QUICKSORT_STACK) {
                    throw new DjVuFileException("Invalid state: sp + 2 >= QUICKSORT_STACK");
                }

                // ----- middle segment (=?) [l1, h1]
                for(int i = l1; i <= h1; i++) {
                    rank[posn[i]] = h1;
                }

                // ----- lower segment (<) [lo, l1[
                if (l1 > lo)
                {
                    for(int i = lo; i < l1; i++) {
                        rank[posn[i]] = l1 - 1;
                    }
                    slo[sp] = lo;
                    shi[sp] = l1 - 1;
                    if (slo[sp] < shi[sp]) {
                        sp++;
                    }
                }

                // ----- upper segment (>) ]h1, hi]
                if (h1 < hi)
                {
                    slo[sp] = h1 + 1;
                    shi[sp] = hi;
                    if (slo[sp] < shi[sp]) {
                        sp++;
                    }
                }
            }
        }
    }

    // pivot -- return suitable pivot
    private int pivot3r(BufferPointer rr, int lo, int hi) {
        int c1;
        int c2;
        int c3;
        if (hi - lo > 256) {
            c1 = pivot3r(rr, lo, (6 * lo + 2 * hi)/8);
            c2 = pivot3r(rr, (5 * lo + 3 * hi)/8, (3 * lo + 5 * hi)/8);
            c3 = pivot3r(rr, (2 * lo + 6 * hi)/8, hi);
        }
        else {
            c1 = rr.getValue(posn[lo]);
            c2 = rr.getValue(posn[(lo + hi)/2]);
            c3 = rr.getValue(posn[hi]);
        }
        // Extract median
        if (c1 > c3) {
            int tmp = c1;
            c1 = c3;
            c3 = tmp;
        }
        if (c2 <= c1) {
            return c1;
        }

        return Math.min(c2, c3);
    }

    // ranksort - a simple insertion sort based on GT
    private void ranksort(int lo, int hi, int depth) {
        int i;
        int j;

        for (i = lo + 1; i <= hi; i++)
        {
            int tmp = posn[i];
            for(j = i - 1; j >= lo && GT(posn[j], tmp, depth); j--) {
                posn[j + 1] = posn[j];
            }
            posn[j + 1] = tmp;
        }

        for(i = lo; i <= hi; i++) {
            rank[posn[i]] = i;
        }
    }

    // GT -- compare suffixes using rank information
    private boolean GT(int p1, int p2, int depth) {
        int r1;
        int r2;
        int twod = depth + depth;

        while (true)
        {
            r1 = rank[p1 + depth];
            r2 = rank[p2 + depth];
            p1 += twod;
            p2 += twod;
            if (r1 != r2) {
                return r1 > r2;
            }

            r1 = rank[p1];
            r2 = rank[p2];
            if (r1 != r2) {
                return r1 > r2;
            }
        }
    }

    // quicksort3d -- Three way quicksort algorithm
    // Sort suffixes based on strings until reaching
    // depth rank at pos+depth
    // The algorithm breaks into ranksort when size is
    // smaller than PRESORT_THRESH
    private void quicksort3d(int lo, int hi, int depth) {
        /* Initialize stack */
        int[] slo = new int[QUICKSORT_STACK];
        int[] shi = new int[QUICKSORT_STACK];
        int[] sd = new int[QUICKSORT_STACK];
        int sp = 1;
        slo[0] = lo;
        shi[0] = hi;
        sd[0] = depth;

        // Recursion elimination loop
        while (--sp >= 0) {
            lo = slo[sp];
            hi = shi[sp];
            depth = sd[sp];
            // Test for insertion sort
            if (depth >= PRESORT_DEPTH)
            {
                for (int i = lo; i <= hi; i++)
                    rank[posn[i]] = hi;
            }
            else if (hi - lo < PRESORT_THRESH)
            {
                int i;
                int j;
                for (i = lo + 1; i <= hi; i++)
                {
                    int tmp = posn[i];
                    for(j = i - 1; j >= lo && GTD(posn[j], tmp, depth); j--) {
                        posn[j + 1] = posn[j];
                    }
                    posn[j + 1] = tmp;
                }

                for(i = hi; i >= lo; i = j)
                {
                    int tmp = posn[i];
                    rank[tmp] = i;
                    for (j = i - 1; j >= lo && !GTD(tmp, posn[j], depth); j--) {
                        rank[posn[j]] = i;
                    }
                }
            }
            else
            {
                int tmp;
                BufferPointer dd = new BufferPointer(data, depth);
                int med = pivot3d(dd, lo, hi);

                // -- positions are organized as follows:
                //   [lo..l1[ [l1..l[ ]h..h1] ]h1..hi]
                //      =        <       >        =
                int l1 = lo;
                int h1 = hi;
                while (dd.getValue(posn[l1]) == med && l1 < h1) {
                    l1++;
                }
                while (dd.getValue(posn[h1]) == med && l1 < h1) {
                    h1--;
                }

                int l = l1;
                int h = h1;
                // -- partition set
                for (;;)
                {
                    while (l <= h)
                    {
                        int c = dd.getValue(posn[l]) - med;
                        if (c > 0) break;
                        if (c == 0) {
                            tmp = posn[l];
                            posn[l] = posn[l1];
                            posn[l1++] = tmp;
                        }
                        l++;
                    }

                    while (l <= h)
                    {
                        int c = dd.getValue(posn[h]) - med;
                        if (c < 0) break;
                        if (c == 0) {
                            tmp = posn[h];
                            posn[h] = posn[h1];
                            posn[h1--] = tmp;
                        }
                        h--;
                    }

                    if (l > h) break;

                    tmp = posn[l];
                    posn[l] = posn[h];
                    posn[h] = tmp;
                }

                // -- reorganize as follows
                //   [lo..l1[ [l1..h1] ]h1..hi]
                //      <        =        >
                tmp = Math.min(l1 - lo, l - l1);
                vswap(lo, l - tmp, tmp, posn);
                l1 = lo + (l - l1);
                tmp = Math.min(hi - h1, h1 - h);
                vswap(hi - tmp + 1, h + 1, tmp, posn);
                h1 = hi - (h1 - h);

                // -- process segments
                // ASSERT(sp+3<QUICKSORT_STACK);
                if (sp + 3 >= QUICKSORT_STACK) {
                    throw new DjVuFileException("Invalid sort state: sp + 3 >= QUICKSORT_STACK");
                }

                // ----- middle segment (=?) [l1, h1]
                l = l1; h = h1;
                if (med == 0) {// special case for marker [slow]
                    for (int i = l; i <= h; i++) {
                        if (posn[i] + depth == size - 1) {
                            tmp = posn[i];
                            posn[i] = posn[l];
                            posn[l] = tmp;
                            rank[tmp] = l++;
                            break;
                        }
                    }
                }

                if (l < h) {
                    slo[sp] = l;
                    shi[sp] = h;
                    sd[sp++] = depth + 1;
                }
                else if (l == h) {
                    rank[posn[h]] = h;
                }

                // ----- lower segment (<) [lo, l1[
                l = lo;
                h = l1 - 1;
                if (l < h) {
                    slo[sp] = l;
                    shi[sp] = h;
                    sd[sp++] = depth;
                }
                else if (l == h) {
                    rank[posn[h]] = h;
                }

                // ----- upper segment (>) ]h1, hi]
                l = h1 + 1;
                h = hi;
                if (l < h) {
                    slo[sp] = l;
                    shi[sp] = h;
                    sd[sp++] = depth;
                }
                else if (l == h) {
                    rank[posn[h]] = h;
                }
            }
        }
    }

    private void vswap(int i, int j, int n, int[] x) {
        while (n-- > 0) {
            int tmp = x[i];
            x[i++] = x[j];
            x[j++] = tmp;
        }
    }

    // pivot3d -- return suitable pivot
    private int pivot3d(BufferPointer rr, int lo, int hi) {
        int c1;
        int c2;
        int c3;

        if (hi - lo > 256)
        {
            c1 = pivot3d(rr, lo, (6 * lo + 2 * hi)/8);
            c2 = pivot3d(rr, (5 * lo + 3 * hi)/8, (3 * lo + 5 * hi)/8);
            c3 = pivot3d(rr, (2 * lo + 6 * hi)/8, hi);
        }
        else
        {
            c1 = rr.getValue(posn[lo]);
            c2 = rr.getValue(posn[(lo + hi)/2]);
            c3 = rr.getValue(posn[hi]);
        }

        // Extract median
        if (c1 > c3) {
            int tmp = c1;
            c1 = c3;
            c3 = tmp;
        }

        if (c2 <= c1) {
            return c1;
        }

        return Math.min(c2, c3);
    }


    // GTD -- compare suffixes using data information
    //  (up to depth PRESORT_DEPTH)
    private boolean GTD(int p1, int p2, int depth) {
        int c1;
        int c2;
        p1 += depth;
        p2 += depth;

        while (depth < PRESORT_DEPTH) {
            // Perform two
            c1 = data[p1];
            c2 = data[p2];
            if (c1 != c2) {
                return c1 > c2;
            }

            c1 = data[p1 + 1];
            c2 = data[p2 + 1];
            p1 += 2;
            p2 += 2;
            depth += 2;
            if (c1 != c2) {
                return c1 > c2;
            }
        }

        if (p1 < size && p2 < size) {
            return false;
        }

        return p1 < p2;
    }

    private void radixsort16() {

        // Initialize frequency array
        int[] ftab = new int[65536];

        // Count occurences
        int c1 = data[0];
        for (int i = 0; i < size - 1; i++) {
            int c2 = data[i + 1];
            ftab[(c1 << 8) | c2] ++;
            c1 = c2;
        }

        // Generate upper position
        for (int i = 1; i < 65536; i++) {
            ftab[i] += ftab[i - 1];
        }

        // Fill rank array with upper bound
        c1 = data[0];
        for (int i = 0; i < size - 2; i++) {
            int c2 = data[i + 1];
            rank[i] = ftab[(c1 << 8) | c2];
            c1 = c2;
        }

        // Fill posn array (backwards)
        c1 = data[size - 2];
        for (int i = size - 3; i >= 0; i--) {
            int c2 = data[i];
            posn[ ftab[(c2 << 8) | c1]-- ] = i;
            c1 = c2;
        }

        // Fixup marker stuff
        c1 = data[size - 2];
        posn[0] = size - 1;
        posn[ ftab[(c1 << 8)] ] = size - 2;
        rank[size - 1] = 0;
        rank[size - 2] = ftab[(c1 << 8)];

        // Extra element
        rank[size] = -1;
    }

    private void radixsort8() {

        // Initialize frequency array
        int[] lo = new int[256];
        int[] hi = new int[256];

        // Count occurences
        for (int i = 0; i < size - 1; i++) {
            hi[data[i]]++;
        }

        // Compute positions (lo)
        int last = 1;
        for (int i = 0; i < 256; i++) {
            lo[i] = last;
            hi[i] = last + hi[i] - 1;
            last = hi[i] + 1;
        }

        for (int i = 0; i < size - 1; i++) {
            posn[ lo[data[i]]++ ] = i;
            rank[ i ] = hi[data[i]];
        }

        // Process marker "$"
        posn[0] = size - 1;
        rank[size - 1] = 0;

        // Extra element
        rank[size] = -1;
    }
}
