package com.sternkn.djvu.file.coders;

import com.sternkn.djvu.file.DjVuFileException;
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
        zpCodec = buildZpCodec("FF");

        assertEquals(0, zpCodec.decoder());
    }

    @Test
    public void testDecoderEndOfFileException() {
        zpCodec = buildZpCodec("");

        for (int ind = 0; ind < 176; ind++) {
            assertEquals(0, zpCodec.decoder());
        }

        Exception exception = assertThrows(DjVuFileException.class, () -> zpCodec.decoder());
        assertEquals("End of djvu file", exception.getMessage());
    }

    @Test
    public void testDecoderLPSCase() {
        zpCodec = buildZpCodec("70");

        assertEquals(1, zpCodec.decoder());
    }

    @Test
    public void testDecoderValidationLowerIndexBorder() {
        zpCodec = buildZpCodec("FF FF BF FE FE E2");

        Exception exception = assertThrows(IllegalArgumentException.class,
                                           () -> zpCodec.decoder(new BitContext(-1)));

        assertEquals("The index should be in range 0 .. 255", exception.getMessage());
    }

    @Test
    public void testDecoderValidationUpperIndexBorder() {
        zpCodec = buildZpCodec("FF FF BF FE FE E2");

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> zpCodec.decoder(new BitContext(256)));

        assertEquals("The index should be in range 0 .. 255", exception.getMessage());
    }

    @Test
    public void testDecoderFastPathPositive() {
        zpCodec = buildZpCodec("70 FA 23 BB FF 4F");
        final int index = 23;
        BitContext bitContext = new BitContext(index);

        int bit = zpCodec.decoder(bitContext);

        assertEquals(1, bit);
        assertEquals(index, bitContext.getValue());
    }

    @Test
    public void testDecoderFastPathNegative() {
        zpCodec = buildZpCodec("70 FA 23 BB FF 4F");
        final int index = 22;
        BitContext bitContext = new BitContext(index);

        int bit = zpCodec.decoder(bitContext);

        assertEquals(0, bit);
        assertEquals(index, bitContext.getValue());
    }

    @Test
    public void testDecoderMPSPositiveWithBitContextIndexUpdate() {
        zpCodec = buildZpCodec("80 00 23 BB FF 4F");
        final int index = 1;
        BitContext bitContext = new BitContext(index);

        int bit = zpCodec.decoder(bitContext);

        assertEquals(1, bit);
        assertEquals(3, bitContext.getValue());
    }

    @Test
    public void testDecoderLPSNegativeWithBitContextIndexUpdate() {
        zpCodec = buildZpCodec("00 1F 23 BB FF 4F");
        final int index = 1;
        BitContext bitContext = new BitContext(index);

        int bit = zpCodec.decoder(bitContext);

        assertEquals(0, bit);
        assertEquals(4, bitContext.getValue());
    }

    @Test
    public void testDecoderLPSPositiveWithBitContextIndexUpdate() {
        zpCodec = buildZpCodec("00 1F 23 BB FF 4F");
        final int index = 2;
        BitContext bitContext = new BitContext(index);

        int bit = zpCodec.decoder(bitContext);

        assertEquals(1, bit);
        assertEquals(3, bitContext.getValue());
    }

    private ZpCodecInputStream buildZpCodec(String data) {
        return new ZpCodecInputStream(new ByteArrayInputStream(HEX_FORMAT.parseHex(data)));
    }
}
