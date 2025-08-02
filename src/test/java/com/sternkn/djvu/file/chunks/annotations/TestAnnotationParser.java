package com.sternkn.djvu.file.chunks.annotations;

import com.sternkn.djvu.file.chunks.Color;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestAnnotationParser {
    private static final String ANNOTATION_SRC =
        """
        q 
        (maparea "http://www.test.com/" "It \\"is a link" (rect 543 2859 408 183 ) (xor ) )
        (maparea (url "http://test.com/" "_blank") "This is an oval" (oval 1068 2853 429 195 ) (xor ))
        (background #FFDDFF ) (zoom page ) (mode bw ) (align center default )
        (maparea "#+1" "Here is a text box"(text 1635 2775 423 216 )(pushpin ) (backclr #FFFF80 ) (border #000000 ) )
        (maparea "" "Arrow" (line 591 3207 1512 3138 ) (arrow ) (none ) )
        """;

    @Test
    public void testParse() {
        List<Node> nodes = AnnotationParser.parse(ANNOTATION_SRC);
        assertNotNull(nodes);
    }

    @Test
    public void testParseColorValidCase() {
        Color color = AnnotationParser.parseColor("#112fAB");

        assertEquals(17, color.getRed());
        assertEquals(47, color.getGreen());
        assertEquals(171, color.getBlue());
    }

    @Test
    public void testParseColorInvalidColorFormat() {
        Exception exception = assertThrows(InvalidAnnotationException.class,
                () -> AnnotationParser.parseColor("#112fAG"));

        assertEquals("Invalid color value: #112fAG", exception.getMessage());
    }

    @Test
    public void testParseColorTextIsNull() {
        Exception exception = assertThrows(InvalidAnnotationException.class,
                () -> AnnotationParser.parseColor(null));

        assertEquals("Text can not be null or blank", exception.getMessage());
    }
}
