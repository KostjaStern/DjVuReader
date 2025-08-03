package com.sternkn.djvu.file.chunks.annotations;

import com.sternkn.djvu.file.chunks.Color;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestAnnotationParser {
    private static final String ANNOTATION_SRC =
        """
        q 
        (maparea "http://www.test.com/" "It \\"is a link" (rect 543 2859 408 183 ) (xor ) )
        (maparea (url "http://test.com/" "_blank") "This is an oval" (oval 1068 2853 429 195 ) (xor ))
        (background #FFDDEE ) (zoom page ) (mode bw ) (align center top )
        (maparea "#+1" "Here is a text box"(text 1635 2775 423 216 )(pushpin ) (backclr #FFFF80 ) (border #000000 ) )
        (maparea "" "Arrow" (line 591 3207 1512 3138 ) (arrow ) (none ) )
        """;

    @Test
    public void testParse() {
        final String text =
        """
            q 
            (maparea "http://www.test.com/" "It \\"is a link" (rect 543 2859 408 183 ) (xor ) )
            (align center top ) www         
        """;

        List<Node> nodes = AnnotationParser.parse(text);
        assertNotNull(nodes);

        List<Node> expectedNodes = List.of(
            new Node(
                List.of("maparea", "\"http://www.test.com/\"", "\"It \\\"is a link\""),
                List.of(
                    new Node(List.of("rect", "543", "2859", "408", "183")),
                    new Node(List.of("xor"))
                )
            ),
            new Node(List.of("align", "center", "top"))
        );
        assertEquals(expectedNodes, nodes);
    }

    @Test
    public void testGetBackgroundColor() {
        AnnotationParser parser = new AnnotationParser(ANNOTATION_SRC);
        BackgroundColor bgColor = parser.getBackgroundColor();

        Color expectedColor = new Color(238, 221, 255);
        assertEquals(expectedColor, bgColor.getColor());
    }

    @Test
    public void testGetInvalidBackgroundColor() {
        AnnotationParser parser = new AnnotationParser("aa (background) ww");

        Exception exception = assertThrows(InvalidAnnotationException.class, parser::getBackgroundColor);
        assertEquals("Invalid background color annotation (without color)", exception.getMessage());
    }

    @Test
    public void testGetNullBackgroundColor() {
        AnnotationParser parser = new AnnotationParser("(mode bw ) (align center top )");
        BackgroundColor bgColor = parser.getBackgroundColor();

        assertNull(bgColor);
    }

    @Test
    public void testGetInitialZoomWithZoomType() {
        AnnotationParser parser = new AnnotationParser("(mode bw ) (zoom page )(align center top )");
        InitialZoom zoom = parser.getInitialZoom();

        assertEquals(ZoomType.PAGE, zoom.getZoomType());
        assertNull(zoom.getZoomFactor());
    }

    @Test
    public void testGetInitialZoomWithZoomFactor() {
        AnnotationParser parser = new AnnotationParser("(mode bw ) (zoom d23 )(align center top )");
        InitialZoom zoom = parser.getInitialZoom();

        assertNull(zoom.getZoomType());
        assertEquals(23, zoom.getZoomFactor());
    }

    @Test
    public void testGetInvalidInitialZoom() {
        AnnotationParser parser = new AnnotationParser(" (zoom d2333 )sss");

        Exception exception = assertThrows(InvalidAnnotationException.class, parser::getInitialZoom);
        assertEquals("Invalid initial zoom annotation value: d2333", exception.getMessage());
    }

    @Test
    public void testGetNullInitialZoom() {
        AnnotationParser parser = new AnnotationParser("(mode bw )");

        assertNull(parser.getInitialZoom());
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
