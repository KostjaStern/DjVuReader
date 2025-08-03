package com.sternkn.djvu.file.chunks.annotations;

import com.sternkn.djvu.file.chunks.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static com.sternkn.djvu.file.utils.NumberUtils.hexToInt;
import static com.sternkn.djvu.file.utils.StringUtils.getChar;

public class AnnotationParser {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationParser.class);

    private static final String COLOR_PATTERN = "^#[0-9a-fA-F]{6}$";
    private static final String ZOOM_FACTOR_PATTERN = "^d\\d{1,3}$";

    private static final String NODE_START = "(";
    private static final String NODE_END = ")";
    private static final String TEXT_START_END_MARKER = "\"";
    private static final String BACKSLASH = "\\";

    private final List<Node> nodes;

    public AnnotationParser(String text) {
        nodes = parse(text);
    }

    public BackgroundColor getBackgroundColor() {
        List<Node> bgNodes = findAnnotationNodes(RecordType.BACKGROUND_COLOR);
        if (bgNodes.isEmpty()) {
            return null;
        }

        if (bgNodes.size() > 1) {
            LOG.warn("We have several background color annotations. We will take the first one into account.");
        }

        Node node = bgNodes.getFirst();

        if (node.getArguments().size() == 1) {
            throw new InvalidAnnotationException("Invalid background color annotation (without color)");
        }

        if (node.getArguments().size() > 2) {
            LOG.warn("It looks like a background color annotation has invalid or unsupported format");
        }

        return new BackgroundColor(parseColor(node.getArguments().get(1)));
    }

    public InitialZoom getInitialZoom() {
        List<Node> zoomNodes = findAnnotationNodes(RecordType.INITIAL_ZOOM);
        if (zoomNodes.isEmpty()) {
            return null;
        }

        if (zoomNodes.size() > 1) {
            LOG.warn("We have several initial zoom annotations. We will take the first one into account.");
        }

        Node node = zoomNodes.getFirst();

        if (node.getArguments().size() == 1) {
            throw new InvalidAnnotationException("Invalid initial zoom annotation (without zoom value)");
        }

        if (node.getArguments().size() > 2) {
            LOG.warn("It looks like an initial zoom annotation has invalid or unsupported format");
        }

        final String zoomValue = node.getArguments().get(1);
        final ZoomType zoomType = ZoomType.of(zoomValue);
        final Integer zoomFactor = parseZoomFactor(zoomValue);

        if (zoomType == null && zoomFactor == null) {
            throw new InvalidAnnotationException("Invalid initial zoom annotation value: " + zoomValue);
        }

        return new InitialZoom(zoomType, zoomFactor);
    }

    private List<Node> findAnnotationNodes(RecordType recordType) {
        final String bgToken = recordType.getToken();
        return nodes.stream()
                .filter(n -> !n.getArguments().isEmpty())
                .filter(n -> bgToken.equals(n.getArguments().getFirst()))
                .toList();
    }

    static List<Node> parse(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<Node> result = new ArrayList<>();
        int index = 0;
        Stack<Node> nodes = new Stack<>();
        StringBuilder tocken = new StringBuilder();
        boolean isTextToken = false;
        boolean isPrevBackslash = false;

        while (index < text.length()) {
            String ch = getChar(text, index);
            index++;
            String stringToken = tocken.toString();

            if (!NODE_START.equals(ch) && nodes.empty()) {
                continue;
            }
            if (NODE_START.equals(ch)) {
                Node node = new Node();
                if (!nodes.empty()) {
                    Node parent = nodes.peek();
                    parent.addChild(node);
                    if (!stringToken.isBlank()) {
                        parent.addArgument(stringToken);
                        tocken = new StringBuilder();
                    }
                }

                nodes.push(node);
                continue;
            }
            if (NODE_END.equals(ch)) {
                if (nodes.empty()) {
                    LOG.warn("It looks like broken annotation text (end node before start)");
                }
                else {
                    Node node = nodes.pop();
                    if (!stringToken.isBlank()) {
                        node.addArgument(stringToken);
                    }
                    if (nodes.empty()) {
                        result.add(node);
                    }
                }
                tocken = new StringBuilder();
                continue;
            }

            if (TEXT_START_END_MARKER.equals(ch)) {
                if (!isPrevBackslash) {
                    isTextToken = !isTextToken;
                }

                isPrevBackslash  = false;
                tocken.append(ch);
                continue;
            }

            if (ch.isBlank() && isTextToken) {
                isPrevBackslash  = false;
                tocken.append(ch);
                continue;
            }

            if (BACKSLASH.equals(ch) && isTextToken) {
                isPrevBackslash = true;
            }

            if (ch.isBlank() && !isTextToken) {
                if (!stringToken.isBlank() && !nodes.empty()) {
                    Node node = nodes.peek();
                    node.addArgument(stringToken);
                    tocken = new StringBuilder();
                }
                continue;
            }

            tocken.append(ch);
        }

        if (!nodes.empty()) {
            LOG.warn("It looks like broken annotation: the count of '(' and ')' characters are not the same");
        }

        return result;
    }

    /**
     *   text - coded color in this format: #RRGGBB
     */
    static Color parseColor(String text) {
        if (text == null || text.isBlank()) {
            throw new InvalidAnnotationException("Text can not be null or blank");
        }

        if (!text.matches(COLOR_PATTERN)) {
            throw new InvalidAnnotationException("Invalid color value: " + text);
        }

        final String red = text.substring(1, 3);
        final String green = text.substring(3, 5);
        final String blue = text.substring(5, 7);

        return new Color(hexToInt(blue), hexToInt(green), hexToInt(red));
    }

    static Integer parseZoomFactor(String text) {
        if (text == null
                || text.isBlank()
                || !text.matches(ZOOM_FACTOR_PATTERN)) {
            return null;
        }

        return Integer.parseInt(text.substring(1));
    }
}
