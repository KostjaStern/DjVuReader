/*
    Copyright (C) 2025 Kostya Stern

    This program is free software; you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation; either version 2 of the License, or (at your option)
    any later version.

    This program is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
    more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc., 51
    Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
*/
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
    public void testGetMapAreasWithRectangle() {
        AnnotationParser parser = new AnnotationParser(
            "www (maparea \"http://www.test.com/\" \"It \\\"is a link\" (rect 543 2859 408 183 ) (xor ) )");
        List<MapArea> mapAreas = parser.getMapAreas();

        assertEquals(1, mapAreas.size());

        assertEquals(new MapArea(
                new MapUrl("http://www.test.com/", null, false),
                "It \\\"is a link",
                new Rectangle(543, 2859, 408, 183)
                        .setOpacity(50)
                        .setBorder(new Border().setXor(true))
        ), mapAreas.getFirst());
    }

    @Test
    public void testGetMapAreasWithOval() {
        AnnotationParser parser = new AnnotationParser(
            "www (maparea (url \"http://test.com/\" \"_blank\") \"This is an oval\" (oval 1068 2853 429 195 ) (xor ))");
        List<MapArea> mapAreas = parser.getMapAreas();

        assertEquals(1, mapAreas.size());

        assertEquals(new MapArea(
            new MapUrl("http://test.com/", "_blank", true),
            "This is an oval",
            new Oval(1068, 2853, 429, 195)
                .setBorder(new Border().setXor(true))
        ), mapAreas.getFirst());
    }

    @Test
    public void testGetMapAreasWithText() {
        AnnotationParser parser = new AnnotationParser(
            "www (maparea \"#+1\" \"Here is a text box\"(text 1635 2775 423 216 )(pushpin ) (backclr #FFFF80 ) (border #000000 ) )");
        List<MapArea> mapAreas = parser.getMapAreas();

        assertEquals(1, mapAreas.size());

        assertEquals(new MapArea(
            new MapUrl("#+1", null, false),
            "Here is a text box",
            new Text(1635, 2775, 423, 216)
                .setPushPin(true)
                .setBackgroundColor(new Color(128, 255, 255))
                .setBorder(new Border().setColor(Color.BLACK))
        ), mapAreas.getFirst());
    }

    @Test
    public void testGetMapAreasWithLine() {
        AnnotationParser parser = new AnnotationParser(
            "www (maparea \"\" \"Arrow\" (line 591 3207 1512 3138 ) (arrow ) (none ) )");
        List<MapArea> mapAreas = parser.getMapAreas();

        assertEquals(1, mapAreas.size());

        assertEquals(new MapArea(
            new MapUrl("", null, false),
            "Arrow",
            new Line(new Point(591, 3207), new Point(1512, 3138))
                .setHasArrow(true)
                .setWidth(1)
                .setColor(Color.BLACK)
                .setBorder(new Border().setNone(true))
        ), mapAreas.getFirst());
    }

    @Test
    public void testGetMapAreasWithPolygon() {
        AnnotationParser parser = new AnnotationParser(
        "(maparea \"#23\" \"Polygon area\" (poly 59 32 151 313 523 623 400 364) (border_avis)(border #203040))");
        List<MapArea> mapAreas = parser.getMapAreas();

        assertEquals(1, mapAreas.size());

        assertEquals(new MapArea(
            new MapUrl("#23", null, false),
            "Polygon area",
            new Polygon(List.of(new Point(59, 32),
                                new Point(151, 313),
                                new Point(523, 623),
                                new Point(400, 364)))
                .setBorderAlwaysVisible(true)
                .setBorder(new Border().setColor(new Color(64, 48, 32)))
        ), mapAreas.getFirst());
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
        assertEquals("Node BACKGROUND_COLOR must have at least one argument", exception.getMessage());
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
    public void testGetInitialDisplayLevel() {
        AnnotationParser parser = new AnnotationParser(" (zoom page ) (mode bw )(align center top )");
        InitialDisplayLevel displayLevel = parser.getInitialDisplayLevel();

        assertEquals(ModeType.BW, displayLevel.getModeType());
    }

    @Test
    public void testGetInvalidInitialDisplayLevel() {
        AnnotationParser parser = new AnnotationParser(" (mode d2333 )sss");

        Exception exception = assertThrows(InvalidAnnotationException.class, parser::getInitialDisplayLevel);
        assertEquals("Invalid initial display level annotation mode value: d2333", exception.getMessage());
    }

    @Test
    public void testGetNullInitialDisplayLevel() {
        AnnotationParser parser = new AnnotationParser(" (zoom page ) (align center top )");

        assertNull(parser.getInitialDisplayLevel());
    }

    @Test
    public void testGetAlignment() {
        AnnotationParser parser = new AnnotationParser(" (zoom page ) (mode bw )(align center top )");
        Alignment alignment = parser.getAlignment();

        assertEquals(AlignmentType.CENTER, alignment.getHorizontal());
        assertEquals(AlignmentType.TOP, alignment.getVertical());
    }

    @Test
    public void testGetNullAlignment() {
        AnnotationParser parser = new AnnotationParser(" (zoom page ) (mode bw )");

        assertNull(parser.getAlignment());
    }

    @Test
    public void testGetInvalidHorizontalAlignment() {
        AnnotationParser parser = new AnnotationParser(" (mode bw )(align center111 top )sss");

        Exception exception = assertThrows(InvalidAnnotationException.class, parser::getAlignment);
        assertEquals("Invalid alignment annotation horizontal type: center111", exception.getMessage());
    }

    @Test
    public void testGetInvalidHorizontalValueAlignment() {
        AnnotationParser parser = new AnnotationParser(" (mode bw )(align top top )sss");

        Exception exception = assertThrows(InvalidAnnotationException.class, parser::getAlignment);
        assertEquals("Invalid horizontal value: TOP", exception.getMessage());
    }

    @Test
    public void testGetInvalidVerticalAlignment() {
        AnnotationParser parser = new AnnotationParser(" (mode bw )(align center toS )sss");

        Exception exception = assertThrows(InvalidAnnotationException.class, parser::getAlignment);
        assertEquals("Invalid alignment annotation vertical type: toS", exception.getMessage());
    }

    @Test
    public void testGetInvalidVerticalValueAlignment() {
        AnnotationParser parser = new AnnotationParser(" (mode bw )(align left right )sss");

        Exception exception = assertThrows(InvalidAnnotationException.class, parser::getAlignment);
        assertEquals("Invalid vertical value: RIGHT", exception.getMessage());
    }
}
