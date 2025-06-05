package com.sternkn.djvu.file.coders;

import com.sternkn.djvu.file.DjVuFileException;
import com.sternkn.djvu.file.DjVuFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
    BSByteStream -> ByteStream -> GPEnabled
 */
public class BSByteStreamDecode {

    private static final Logger LOG = LoggerFactory.getLogger(BSByteStreamDecode.class);

    // Limits on block sizes
    private static final long MAX_BLOCK_SIZE = 4096 * 1024; // 4M
    private static final long MIN_BLOCK_SIZE = 10 * 1024;   // 10K

    private long offset;
    private int bptr;
    private int blockSize; // unsigned int
    private int size;
    private byte[] bitContext; // BitContext ctx[300];

    private final DjVuFileReader fileReader;
    private final ZpCodecDecode zpCodecDecode;

    /** Context variable.
     Variables of type #BitContext# hold a single byte describing how to encode
     or decode message bits with similar statistical properties.  This single
     byte simultaneously represents the current estimate of the bit probability
     distribution (which is determined by the frequencies of #1#s and #0#s
     already coded with this context) and the confidence in this estimate
     (which determines how fast the estimate can change.)

     A coding program typically allocates hundreds of context variables.  Each
     coding context is initialized to zero before encoding or decoding.  Value
     zero represents equal probabilities for #1#s and #0#s with a minimal
     confidence and therefore a maximum adaptation speed.  Each message bit is
     encoded using a coding context determined as a function of previously
     encoded message bits.  The decoder therefore can examine the previously
     decoded message bits and decode the current bit using the same context as
     the encoder.  This is critical for proper decoding.
     */
    // typedef unsigned char  BitContext;

//    fields from BSByteStream.h
//
//    ByteStream *bs;
//    GP<ByteStream> gbs;
//    unsigned char  *data;
//    GPBuffer<unsigned char> gdata;
//    // Coder
//    GP<ZPCodec> gzp;
//    BitContext ctx[300];

//    BSByteStream::BSByteStream(GP<ByteStream> xbs) : offset(0), bptr(0), blocksize(0), size(0), bs(xbs), gbs(xbs), gdata(data,0)
//    {
//        // Initialize context array
//        memset(ctx, 0, sizeof(ctx));
//    }


    public BSByteStreamDecode(DjVuFileReader fileReader) {
        this.fileReader = fileReader;

        this.offset = 0;
        this.bptr = 0;
        this.blockSize = 0;
        this.size = 0;
        this.bitContext = new byte[300];

        this.zpCodecDecode = new ZpCodecDecode(fileReader);
        decode();
    }

    /*
        see  int decode_raw(ZPCodec &zp, int bits) implementation from BSByteStream.cpp
     */
    private int decodeRaw(int bits) {
        int n = 1;
        final int m = (1<<bits);
        while (n < m) {
            final int b = this.zpCodecDecode.decoder();
            n = (n<<1) | b;
        }
        return n - m;
    }

    private int decode() {
        int i;

        this.size = decodeRaw(24);
        LOG.debug("Decoded block size = {}", this.size);

        if (this.size > MAX_BLOCK_SIZE) {
            throw new DjVuFileException("Too big BZZ block size: " + this.size +
                    ". It should be between 10K and 4M");
        }

        return 0;
    }
}
