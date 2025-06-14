package com.sternkn.djvu.file.coders;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class TestZpCodecInputStream {

    private static final HexFormat HEX_FORMAT = HexFormat.ofDelimiter(" ");

    private ZpCodecInputStream zpCodec;

    @Test
    public void testDecoderMPSCase() {
        zpCodec = buildCodec("FF");

        assertEquals(0, zpCodec.decoder());
    }

    @Test
    public void testDecoderLPSCase() {
        zpCodec = buildCodec("70");

        assertEquals(1, zpCodec.decoder());
    }

    @Test
    public void testDecoderValidationLowerIndexBorder() {
        zpCodec = buildCodec("FF FF BF FE FE E2");

        Exception exception = assertThrows(IllegalArgumentException.class,
                                           () -> zpCodec.decoder(new BitContext(-1)));

        assertEquals("The index should be in range 0 .. 255", exception.getMessage());
    }

    @Test
    public void testDecoderValidationUpperIndexBorder() {
        zpCodec = buildCodec("FF FF BF FE FE E2");

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> zpCodec.decoder(new BitContext(260)));

        assertEquals("The index should be in range 0 .. 255", exception.getMessage());
    }

    @Test
    public void testDecoderFastPathPositive() {
        zpCodec = buildCodec("70 FA 23 BB FF 4F");
        final int index = 23;
        BitContext bitContext = new BitContext(index);

        int bit = zpCodec.decoder(bitContext);

        assertEquals(1, bit);
        assertEquals(index, bitContext.getValue());
    }

    @Test
    public void testDecoderFastPathNegative() {
        zpCodec = buildCodec("70 FA 23 BB FF 4F");
        final int index = 22;
        BitContext bitContext = new BitContext(index);

        int bit = zpCodec.decoder(bitContext);

        assertEquals(0, bit);
        assertEquals(index, bitContext.getValue());
    }

    @Test
    public void testDecoderMPSPositiveWithBitContextIndexUpdate() {
        zpCodec = buildCodec("80 00 23 BB FF 4F");
        final int index = 1;
        BitContext bitContext = new BitContext(index);

        int bit = zpCodec.decoder(bitContext);

        assertEquals(1, bit);
        assertEquals(3, bitContext.getValue());
    }

    @Test
    public void testDecoderLPSNegativeWithBitContextIndexUpdate() {
        zpCodec = buildCodec("00 1F 23 BB FF 4F");
        final int index = 1;
        BitContext bitContext = new BitContext(index);

        int bit = zpCodec.decoder(bitContext);

        assertEquals(0, bit);
        assertEquals(4, bitContext.getValue());
    }

    @Test
    public void testDecoderLPSPositiveWithBitContextIndexUpdate() {
        zpCodec = buildCodec("00 1F 23 BB FF 4F");
        final int index = 2;
        BitContext bitContext = new BitContext(index);

        int bit = zpCodec.decoder(bitContext);

        assertEquals(1, bit);
        assertEquals(3, bitContext.getValue());
    }

    private ZpCodecInputStream buildCodec(String data) {
        return new ZpCodecInputStream(new ByteArrayInputStream(HEX_FORMAT.parseHex(data)));
    }
}
