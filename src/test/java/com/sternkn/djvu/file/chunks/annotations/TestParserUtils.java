package com.sternkn.djvu.file.chunks.annotations;

import com.sternkn.djvu.file.chunks.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestParserUtils {
    @Test
    public void testParseColorValidCase() {
        Color color = ParserUtils.parseColor("#112fAB");

        assertEquals(17, color.getRed());
        assertEquals(47, color.getGreen());
        assertEquals(171, color.getBlue());
    }

    @Test
    public void testParseColorInvalidColorFormat() {
        Exception exception = assertThrows(InvalidAnnotationException.class,
                () -> ParserUtils.parseColor("#112fAG"));

        assertEquals("Invalid color value: #112fAG", exception.getMessage());
    }

    @Test
    public void testParseColorTextIsNull() {
        Exception exception = assertThrows(InvalidAnnotationException.class,
                () -> ParserUtils.parseColor(null));

        assertEquals("Text can not be null or blank", exception.getMessage());
    }
}
