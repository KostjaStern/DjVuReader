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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static com.sternkn.djvu.file.utils.NumberUtils.toInt;
import static com.sternkn.djvu.file.chunks.annotations.ParserUtils.findNodes;
import static com.sternkn.djvu.file.chunks.annotations.ParserUtils.isTrue;
import static com.sternkn.djvu.file.chunks.annotations.ParserUtils.parseColorNode;
import static com.sternkn.djvu.file.chunks.annotations.ParserUtils.parseIntNode;

public abstract class Area {
    private static final Logger LOG = LoggerFactory.getLogger(Area.class);

    private final static Predicate<Integer> SHADOW_VALIDATOR = (Integer value) -> value >= 1 && value <= 32;
    private final static Predicate<Integer> OPACITY_VALIDATOR = (Integer value) -> value >= 0 && value <= 100;
    private final static Predicate<Integer> WIDTH_VALIDATOR = (Integer value) -> value > 0;

    protected AreaType type;
    protected Border border;

    protected Area(AreaType type) {
        this.type = type;
    }

    public Border getBorder() {
        return border;
    }
    public Area setBorder(Border border) {
        this.border = border;
        return this;
    }

    public AreaType getType() {
        return type;
    }

    public static Area parseArea(Node node) {
        AreaType type = AreaType.fromToken(node);
        if (type == null) {
            throw new IllegalArgumentException("Invalid area type");
        }

        Area area = switch (type) {
            case RECTANGLE -> parseRectangle(node);
            case OVAL -> parseOval(node);
            case TEXT_BOX -> parseText(node);
            case LINE -> parseLine(node);
            case POLYGON ->  parsePolygon(node);
        };

        final Border border = parseBorder(node);
        area.setBorder(border);

        return area;
    }

    private static Rectangle parseRectangle(Node node) {
        List<Node> rects = findNodes(node.getChildren(), TagType.RECTANGLE);
        if (rects.size() > 1) {
            LOG.warn("We have more than one rectangle annotation. We will take the first one into account.");
        }

        Node rect = rects.getFirst();
        if (rect.getArguments().size() != 5) {
            throw new InvalidAnnotationException("Rectangle should have 4 arguments");
        }

        final int xmin = toInt(rect.getArguments().get(1));
        final int ymin = toInt(rect.getArguments().get(2));
        final int width = toInt(rect.getArguments().get(3));
        final int height = toInt(rect.getArguments().get(4));

        final Color highlightColor = parseColorNode(node.getChildren(), TagType.HIGHLIGHT);
        final int opacity = parseIntNode(node, TagType.OPACITY, OPACITY_VALIDATOR, 50);

        return new Rectangle(xmin, ymin, width, height)
            .setBorderAlwaysVisible(isTrue(node, TagType.BORDER_AVIS))
            .setOpacity(opacity)
            .setHighlightedColor(highlightColor);
    }

    private static Line parseLine(Node node) {
        List<Node> lines = findNodes(node.getChildren(), TagType.LINE);
        if (lines.size() > 1) {
            LOG.warn("We have more than one line tag. We will take into account only the first one.");
        }

        Node line = lines.getFirst();
        if (line.getArguments().size() != 5) {
            throw new InvalidAnnotationException("Line should have 4 arguments");
        }

        final int x0 = toInt(line.getArguments().get(1));
        final int y0 = toInt(line.getArguments().get(2));
        final int x1 = toInt(line.getArguments().get(3));
        final int y1 = toInt(line.getArguments().get(4));

        final Color lineColor = parseColorNode(node.getChildren(), TagType.LINE_COLOR, Color.BLACK);
        final int width = parseIntNode(node, TagType.WIDTH, WIDTH_VALIDATOR, 1);

        return new Line(new Point(x0, y0), new Point(x1, y1))
                .setHasArrow(isTrue(node, TagType.ARROW))
                .setWidth(width)
                .setColor(lineColor);
    }

    private static Polygon parsePolygon(Node node) {
        List<Node> polygons = findNodes(node.getChildren(), TagType.POLYGON);
        if (polygons.size() > 1) {
            LOG.warn("We have more than one polygons tag. We will take into account only the first one.");
        }

        Node polygon = polygons.getFirst();
        int argSize = polygon.getArguments().size();
        if (argSize < 5) {
            throw new InvalidAnnotationException("Polygon should have more than 4 arguments. (argSize = "
                    + argSize + ")");
        }
        if (argSize % 2 == 0) {
            throw new InvalidAnnotationException("Polygon should have even number of arguments.");
        }

        final int pointSize = (argSize - 1) / 2;
        final List<Point> points = new ArrayList<>(pointSize);
        for (int ind = 0; ind < pointSize; ind++) {
            final int x = toInt(polygon.getArguments().get(2 * ind + 1));
            final int y = toInt(polygon.getArguments().get(2 * ind + 2));
            points.add(new Point(x, y));
        }

        return new Polygon(points)
            .setBorderAlwaysVisible(isTrue(node, TagType.BORDER_AVIS));
    }

    private static Text parseText(Node node) {
        List<Node> texts = findNodes(node.getChildren(), TagType.TEXT_BOX);
        if (texts.size() > 1) {
            LOG.warn("We have more than one text annotation. We will take the first one into account.");
        }

        Node text = texts.getFirst();
        if (text.getArguments().size() != 5) {
            throw new InvalidAnnotationException("Text block should have 4 arguments");
        }

        final int xmin = toInt(text.getArguments().get(1));
        final int ymin = toInt(text.getArguments().get(2));
        final int width = toInt(text.getArguments().get(3));
        final int height = toInt(text.getArguments().get(4));

        Color backgroundColor = parseColorNode(node.getChildren(), TagType.BACK_COLOR);
        Color textColor = parseColorNode(node.getChildren(), TagType.TEXT_COLOR);

        return new Text(xmin, ymin, width, height)
                .setBackgroundColor(backgroundColor)
                .setTextColor(textColor)
                .setPushPin(isTrue(node, TagType.PUSH_PIN));
    }

    private static Oval parseOval(Node node) {
        List<Node> ovals = findNodes(node.getChildren(), TagType.OVAL);
        if (ovals.size() > 1) {
            LOG.warn("We have more than one oval annotation. We will take the first one into account.");
        }

        Node oval = ovals.getFirst();
        if (oval.getArguments().size() != 5) {
            throw new InvalidAnnotationException("Oval should have 4 arguments");
        }

        final int xmin = toInt(oval.getArguments().get(1));
        final int ymin = toInt(oval.getArguments().get(2));
        final int width = toInt(oval.getArguments().get(3));
        final int height = toInt(oval.getArguments().get(4));

        return new Oval(xmin, ymin, width, height)
            .setBorderAlwaysVisible(isTrue(node, TagType.BORDER_AVIS));
    }

    private static Border parseBorder(Node node) {
        return new Border()
            .setNone(isTrue(node, TagType.NO_BORDER))
            .setXor(isTrue(node, TagType.XOR))
            .setColor(parseColorNode(node.getChildren(), TagType.BORDER))
            .setShadowIn(parseIntNode(node, TagType.SHADOW_IN, SHADOW_VALIDATOR))
            .setShadowOut(parseIntNode(node, TagType.SHADOW_OUT, SHADOW_VALIDATOR))
            .setShadowEIn(parseIntNode(node, TagType.SHADOW_EIN, SHADOW_VALIDATOR))
            .setShadowEOut(parseIntNode(node, TagType.SHADOW_EOUT, SHADOW_VALIDATOR));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Area other)) {
            return false;
        }

        return type == other.getType()
            && Objects.equals(this.border, other.border);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, border);
    }
}
