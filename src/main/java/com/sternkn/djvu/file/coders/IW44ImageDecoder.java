package com.sternkn.djvu.file.coders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IW44ImageDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(IW44ImageDecoder.class);

    public record BandBucket(int start, int size) {
    }

    private static final BandBucket[] BAND_BUCKETS = {
        // Code first bucket and number of buckets in each band
        new BandBucket(0, 1), // -- band zero contains all lores info
        new BandBucket(1, 1),
        new BandBucket(2, 1),
        new BandBucket(3, 1),
        new BandBucket(4, 4),
        new BandBucket(8, 4),
        new BandBucket(12, 4),
        new BandBucket(16, 16),
        new BandBucket(32, 16),
        new BandBucket(48, 16)
    };

    // Parameters for IW44 wavelet.
    // - iw_quant: quantization for all 16 sub-bands
    // - iw_norm: norm of all wavelets (for db estimation)
    // - iw_border: pixel border required to run filters
    // - iw_shift: scale applied before decomposition
    private static final int IW_QUANT[] = {
            0x004000, 0x008000, 0x008000, 0x010000,
            0x010000, 0x010000, 0x020000, 0x020000,
            0x020000, 0x040000, 0x040000, 0x040000,
            0x080000, 0x040000, 0x040000, 0x080000
    };

    private static final int IW_BORDER = 3;
    private static final int IW_SHIFT  = 6;
    private static final int IW_ROUND  = (1 << (IW_SHIFT - 1));

    private static final int ZERO_COEFF = 1;
    private static final int ACTIVE_COEFF = 2;
    private static final int NEW_COEFF = 4;
    private static final int UNK_COEFF = 8;

//    enum CoefficientState {
//        ZERO,    // = 1 this coeff never hits this bit
//        ACTIVE,  // = 2 this coeff is already active
//        NEW,     // = 4 this coeff is becoming active
//        UNK      // = 8 this coeff may become active
//    };

    // Data
    private IW44ImageMap map;                  // working map

    private int curband;      // current band
    private int curbit;       // current bitplane

    private int[] quant_hi;   // (size - 10) quantization for bands 1 to 9
    private int[] quant_lo;   // (size - 16) quantization for band 0.

    // bucket state
    private int[] coeffState; // 256
    private int[] bucketState; // 16

    // coding context
    private BitContext[] ctxStart;    // [32]
    private BitContext[][] ctxBucket; // [10][8]
    private BitContext ctxMant;
    private BitContext ctxRoot;

    // IW44Image::Codec::Codec(IW44Image::Map &xmap) : map(xmap), curband(0), curbit(1)
    public IW44ImageDecoder(IW44ImageMap map) {
        this.coeffState = new int[256];
        this.bucketState = new int[16];

        this.map = map;
        curband = 0;
        curbit = 1;

        quant_hi = new int[10];
        quant_lo = new int[16];

// Initialize quantification
        int j;
        int ind = 0;
        int offset = 0;
        BufferPointer q = new BufferPointer(IW_QUANT);
        // -- lo coefficients
        for (j = 0; ind < 4; j++) {
            quant_lo[ind] = q.getValue(offset);
            ind++;
            offset++;
        }
        for (j = 0; j < 4; j++) {
            quant_lo[ind] = q.getValue(offset);
            ind++;
        }

        offset += 1;
        for (j = 0; j < 4; j++) {
            quant_lo[ind] = q.getValue(offset);
            ind++;
        }

        offset += 1;
        for (j = 0; j < 4; j++) {
            quant_lo[ind] = q.getValue(offset);
            ind++;
        }

        offset += 1;
        // -- hi coefficients
        quant_hi[0] = 0;
        for (ind = 1; ind < 10; ind++) {
            quant_hi[ind] = q.getValue(offset);
            offset++;
        }

        // Initialize coding contexts
        ctxBucket  = new BitContext[10][8];
        for (int i = 0; i < ctxBucket.length; i++) {
            for (j = 0; j < ctxBucket[i].length; j++) {
                ctxBucket[i][j] = new BitContext();
            }
        }

        ctxStart = new BitContext[32];
        for (int i = 0; i < ctxStart.length; i++) {
            ctxStart[i] = new BitContext();
        }

        ctxMant = new BitContext();
        ctxRoot = new BitContext();
    }


    public int code_slice(ZPCodecDecoder zpDecoder) {
        // Check that code_slice can still run
        if (curbit < 0) {
            return 0;
        }

        // Perform coding
        if (!is_null_slice(curbit, curband)) {
            for (int blockno = 0; blockno < map.nb; blockno++)
            {
                int fbucket = BAND_BUCKETS[curband].start();
                int nbucket = BAND_BUCKETS[curband].size();
                decode_buckets(zpDecoder, curbit, curband, map.getBlock(blockno), fbucket, nbucket);
            }
        }

        return finish_code_slice();
    }

    private int finish_code_slice()
    {
        // Reduce quantization threshold
        quant_hi[curband] = quant_hi[curband] >> 1;
        if (curband == 0) {
            for (int i = 0; i < 16; i++) {
                quant_lo[i] = quant_lo[i] >> 1;
            }
        }

        // Proceed to the next slice
        if (++curband >= BAND_BUCKETS.length) // (int)(sizeof(bandbuckets)/sizeof(bandbuckets[0]))
        {
            curband = 0;
            curbit += 1;
            if (quant_hi[BAND_BUCKETS.length - 1] == 0) { // (sizeof(bandbuckets)/sizeof(bandbuckets[0]))
                // All quantization thresholds are null
                curbit = -1;
                return 0;
            }
        }
        return 1;
    }

    private boolean is_null_slice(int bit, int band) {
        if (band == 0)
        {
            boolean is_null = true;
            for (int i = 0; i < 16; i++)
            {
                int threshold = quant_lo[i];
                coeffState[i] = ZERO_COEFF;
                if (threshold > 0 && threshold < 0x8000) {
                    coeffState[i] = UNK_COEFF;
                    is_null = false;
                }
            }
            return is_null;
        }
        else {
            int threshold = quant_hi[band];
            return !(threshold > 0 && threshold < 0x8000);
        }
    }

    private void decode_buckets(ZPCodecDecoder zp, int bit, int band,
                                IW44ImageBlock blk, int fbucket, int nbucket) {
        // compute state of all coefficients in all buckets
        int bbstate = decode_prepare(fbucket, nbucket, blk);
        // code root bit
        if ((nbucket < 16) || ((bbstate & ACTIVE_COEFF) != 0))
        {
            bbstate |= NEW_COEFF;
        }
        else if ((bbstate & UNK_COEFF) != 0)
        {
            if (zp.decoder(ctxRoot) != 0) {
                bbstate |= NEW_COEFF;
            }

            // LOG.debug("bbstate[bit = {}, band = {}] = {}", bit, band, bbstate);
        }

        // code bucket bits
        if ((bbstate & NEW_COEFF) != 0) {
            for (int buckno = 0; buckno < nbucket; buckno++) {
                // Code bucket bit
                if ((this.bucketState[buckno] & UNK_COEFF) != 0) {
                    // Context
                    int ctx = 0;
                    if (band > 0) {
                        int k = (fbucket + buckno) << 2;
                        BufferPointer b = blk.data(k >> 4);
                        if (b != null) {
                            k = k & 0xf;
                            if (b.getValue(k) != 0) {
                                ctx += 1;
                            }
                            if (b.getValue(k + 1) != 0) {
                                ctx += 1;
                            }
                            if (b.getValue(k + 2) != 0) {
                                ctx += 1;
                            }
                            if (ctx < 3 && b.getValue(k + 3) != 0) {
                                ctx += 1;
                            }
                        }
                    }

                    if ((bbstate & ACTIVE_COEFF) != 0) {
                        ctx |= 4;
                    }

                    // Code
                    if (zp.decoder(ctxBucket[band][ctx]) != 0) {
                        this.bucketState[buckno] |= NEW_COEFF;
                    }

//                    LOG.debug("bucketstate[bit = {}, band = {}, buck = {}] = {}",
//                            bit, band, buckno, this.bucketState[buckno]);
                }
            }
        }
        // code new active coefficient (with their sign)
        if ((bbstate & NEW_COEFF) != 0)
        {
            int thres = quant_hi[band];
            BufferPointer cstate = new BufferPointer(this.coeffState);
            for (int buckno = 0; buckno < nbucket; buckno++, cstate = cstate.shiftPointer(16)) //  cstate+=16
                if ((this.bucketState[buckno] & NEW_COEFF) != 0)
                {
                    int i;
                    BufferPointer pcoeff = blk.data(fbucket + buckno);
                    if (pcoeff == null)
                    {
                        pcoeff = blk.data(fbucket + buckno, map);
                        // time to fill cstate[0..15]
                        if (fbucket == 0) // band zero
                        {
                            for (i = 0; i < 16; i++) {
                                if (cstate.getValue(i) != ZERO_COEFF) {
                                    cstate.setValue(i, UNK_COEFF);
                                }
                            }
                        }
                        else
                        {
                            for (i = 0; i < 16; i++) {
                                cstate.setValue(i, UNK_COEFF);
                            }
                        }
                    }
// #ifndef NOCTX_EXPECT
                    int gotcha = 0;
                    final int maxgotcha = 7;
                    for (i = 0; i < 16; i++) {
                        if ((cstate.getValue(i) & UNK_COEFF) != 0) {
                            gotcha += 1;
                        }
                    }
// #endif
                    for (i = 0; i < 16; i++)
                    {
                        if ((cstate.getValue(i) & UNK_COEFF) != 0)
                        {
                            // find lores threshold
                            if (band == 0) {
                                thres = quant_lo[i];
                            }
                            // prepare context
                            int ctx = 0;
// #ifndef NOCTX_EXPECT
                            if (gotcha >= maxgotcha) {
                                ctx = maxgotcha;
                            }
                            else {
                                ctx = gotcha;
                            }
// #endif
// #ifndef NOCTX_ACTIVE
                            if ((this.bucketState[buckno] & ACTIVE_COEFF) != 0) {
                                ctx |= 8;
                            }
// #endif
                            // code difference bit
                            if (zp.decoder( ctxStart[ctx] ) != 0)
                            {
                                // cstate[i] |= NEW;
                                cstate.setValue(i, cstate.getValue(i) | NEW_COEFF);

                                int halfthres = thres >> 1;
                                int coeff = thres + halfthres - (halfthres >> 2);
                                if (zp.IWdecoder() != 0) {
                                    // pcoeff[i] = -coeff;
                                    pcoeff.setValue(i, -coeff);
                                }
                                else {
                                    // pcoeff[i] = coeff;
                                    pcoeff.setValue(i, coeff);
                                }
                            }
// #ifndef NOCTX_EXPECT
                            if ((cstate.getValue(i) & NEW_COEFF) != 0) {
                                gotcha = 0;
                            }
                            else if (gotcha > 0) {
                                gotcha -= 1;
                            }

//                            LOG.debug("coeffstate[bit = {}, band = {}, buck = {}, c = {}] = {}",
//                                    bit, band, buckno, i, cstate.getValue(i));

                        }
                    }
                }
        }

        // code mantissa bits
        if ((bbstate & ACTIVE_COEFF) != 0)
        {
            int thres = quant_hi[band];
            BufferPointer cstate = new BufferPointer(this.coeffState); // coeffstate
            for (int buckno = 0; buckno < nbucket; buckno++, cstate = cstate.shiftPointer(16)) // cstate+=16
                if ((this.bucketState[buckno] & ACTIVE_COEFF) != 0) // bucketstate[buckno] & ACTIVE
                {
                    BufferPointer pcoeff = blk.data(fbucket + buckno);
                    for (int i = 0; i < 16; i++)
                        if ((cstate.getValue(i) & ACTIVE_COEFF) != 0)
                        {
                            int coeff = pcoeff.getValue(i);
                            if (coeff < 0) {
                                coeff = -coeff;
                            }
                            // find lores threshold
                            if (band == 0) {
                                thres = quant_lo[i];
                            }
                            // adjust coefficient
                            if (coeff <= 3 * thres)
                            {
                                // second mantissa bit
                                coeff = coeff + (thres >> 2);
                                if (zp.decoder(ctxMant) != 0) {
                                    coeff = coeff + (thres >> 1);
                                }
                                else {
                                    coeff = coeff - thres + (thres >> 1);
                                }
                            }
                            else
                            {
                                if (zp.IWdecoder() != 0) {
                                    coeff = coeff + (thres >> 1);
                                }
                                else {
                                    coeff = coeff - thres + (thres >> 1);
                                }
                            }
                            // store coefficient
                            if (pcoeff.getValue(i) > 0) {
                                pcoeff.setValue(i, coeff);
                            }
                            else {
                                pcoeff.setValue(i, -coeff);
                            }
                        }
                }
        }
    }


    /*
    enum CoefficientState {
        ZERO,    // = 1 this coeff never hits this bit
        ACTIVE,  // = 2 this coeff is already active
        NEW,     // = 4 this coeff is becoming active
        UNK      // = 8 this coeff may become active
    };
     */
    private int decode_prepare(int fbucket, int nbucket, IW44ImageBlock blk) {
        int bbstate = 0;
        BufferPointer cstate = new BufferPointer(this.coeffState, 0); // coeffstate
        // int cstate = 0;
        if (fbucket != 0) {
            // Band other than zero
            for (int buckno = 0; buckno < nbucket; buckno++, cstate = cstate.shiftPointer(16) ) // cstate+=16
            {
                int bstatetmp = 0;
                BufferPointer pcoeff = blk.data(fbucket + buckno);
                if (pcoeff == null)
                {
                    // cstate[0..15] will be filled later
                    bstatetmp = UNK_COEFF;
                }
                else
                {
                    for (int i = 0; i < 16; i++)
                    {
                        int cstatetmp = UNK_COEFF;
                        if (pcoeff.getValue(i) != 0) {
                            cstatetmp = ACTIVE_COEFF;
                        }
                        // cstate[i] = cstatetmp;
                        cstate.setValue(i, cstatetmp);
                        bstatetmp |= cstatetmp;
                    }
                }
                // bucketstate[buckno] = bstatetmp;
                this.bucketState[buckno] = bstatetmp;
                bbstate |= bstatetmp;
            }
        }
        else
        {
            // Band zero ( fbucket==0 implies band==zero and nbucket==1 )
            BufferPointer pcoeff = blk.data(0);
            if (pcoeff == null)
            {
                // cstate[0..15] will be filled later
                bbstate = UNK_COEFF;
            }
            else
            {
                for (int i = 0; i < 16; i++)
                {
                    int cstatetmp = cstate.getValue(i);
                    if (cstatetmp != ZERO_COEFF)
                    {
                        cstatetmp = UNK_COEFF;
                        if (pcoeff.getValue(i) != 0) {
                            cstatetmp = ACTIVE_COEFF;
                        }
                    }
                    // cstate[i] = cstatetmp;
                    cstate.setValue(i, cstatetmp);
                    bbstate |= cstatetmp;
                }
            }
            // bucketstate[0] = bbstate;
            this.bucketState[0] = bbstate;
        }
        return bbstate;
    }
}
