package com.sternkn.djvu.file.chunks.annotations;

import com.sternkn.djvu.file.chunks.Color;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestParserUtils {

    private static final Node ARROW = new Node(List.of("arrow"));
    private static final Node BACKGROUND = new Node(List.of("background", "#FFDDEE"));
    private static final Node ZOOM = new Node(List.of("zoom", "page"));
    private static final Node MAP_AREA = new Node(List.of("maparea", "", "Arrow"), List.of(ARROW));

    @Test
    public void testFindNodes() {
        List<Node> nodes = List.of(ARROW, BACKGROUND, ZOOM);

        assertEquals(List.of(BACKGROUND), ParserUtils.findNodes(nodes, TagType.BACKGROUND_COLOR));
    }

    @Test
    public void testNotFoundFindNodes() {
        List<Node> nodes = List.of(ARROW, BACKGROUND, ZOOM);

        assertTrue(ParserUtils.findNodes(nodes, TagType.LINE_COLOR).isEmpty());
    }

    @Test
    public void testIsTruePositive() {
        assertTrue(ParserUtils.isTrue(MAP_AREA, TagType.ARROW));
    }

    @Test
    public void testIsTrueNegative() {
        assertFalse(ParserUtils.isTrue(MAP_AREA, TagType.NO_BORDER));
    }

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
