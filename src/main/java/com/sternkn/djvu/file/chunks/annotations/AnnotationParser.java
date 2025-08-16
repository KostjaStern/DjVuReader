package com.sternkn.djvu.file.chunks.annotations;

import com.sternkn.djvu.file.chunks.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static com.sternkn.djvu.file.chunks.annotations.ParserUtils.findNodes;
import static com.sternkn.djvu.file.chunks.annotations.ParserUtils.parseColorNode;
import static com.sternkn.djvu.file.utils.StringUtils.getChar;

public class AnnotationParser {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationParser.class);

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
        Color color = parseColorNode(nodes, TagType.BACKGROUND_COLOR);
        return color == null ? null : new BackgroundColor(color);
    }

    public InitialZoom getInitialZoom() {
        List<Node> zoomNodes = findAnnotationNodes(TagType.INITIAL_ZOOM);
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

    public InitialDisplayLevel  getInitialDisplayLevel() {
        List<Node> displayNodes = findAnnotationNodes(TagType.INITIAL_DISPLAY_LEVEL);
        if (displayNodes.isEmpty()) {
            return null;
        }

        if (displayNodes.size() > 1) {
            LOG.warn("We have several initial display level annotations. We will take the first one into account.");
        }

        Node node = displayNodes.getFirst();

        if (node.getArguments().size() == 1) {
            throw new InvalidAnnotationException("Invalid initial display level annotation (without mode value)");
        }

        if (node.getArguments().size() > 2) {
            LOG.warn("It looks like an initial display level annotation has invalid or unsupported format");
        }

        final String modeValue = node.getArguments().get(1);
        final ModeType modeType = ModeType.of(modeValue);

        if (modeType == null) {
            throw new InvalidAnnotationException("Invalid initial display level annotation mode value: " + modeValue);
        }

        return new InitialDisplayLevel(modeType);
    }

    public Alignment getAlignment() {
        List<Node> alignmentNodes = findAnnotationNodes(TagType.ALIGNMENT);
        if (alignmentNodes.isEmpty()) {
            return null;
        }

        if (alignmentNodes.size() > 1) {
            LOG.warn("We have several alignment annotations. We will take the first one into account.");
        }

        Node node = alignmentNodes.getFirst();

        if (node.getArguments().size() < 3) {
            throw new InvalidAnnotationException("Invalid alignment annotation (without horzalign and/or vertalign)");
        }

        if (node.getArguments().size() > 3) {
            LOG.warn("It looks like an alignment annotation has invalid or unsupported format");
        }

        final String horzalignValue = node.getArguments().get(1);
        final String vertalignValue = node.getArguments().get(2);

        final AlignmentType horzType = AlignmentType.of(horzalignValue);
        final AlignmentType vertType = AlignmentType.of(vertalignValue);

        if (horzType == null) {
            throw new InvalidAnnotationException("Invalid alignment annotation horizontal type: " + horzalignValue);
        }
        if (vertType == null) {
            throw new InvalidAnnotationException("Invalid alignment annotation vertical type: " + vertalignValue);
        }

        return new Alignment(horzType, vertType);
    }

    public List<MapArea> getMapAreas() {
        List<Node> mapAreaNodes = findAnnotationNodes(TagType.MAP_AREA);
        return mapAreaNodes.stream().map(this::parseMapArea).toList();
    }

    private MapArea parseMapArea(Node node) {

        MapUrl url = parseMapUrl(node);
        int nodeArgSize = node.getArguments().size();
        String comment;

        if (url.isObject()) {
            comment = nodeArgSize > 1 ? node.getArguments().get(1) : null;
        }
        else {
            comment = nodeArgSize > 2 ? node.getArguments().get(2) : null;
        }

        Area area = Area.parseArea(node);

        return new MapArea(url, comment, area);
    }

    private MapUrl parseMapUrl(Node node) {
        List<Node> urlNodes = findNodes(node.getChildren(), TagType.URL);
        if (urlNodes.size() > 1) {
            LOG.warn("It looks like an invalid map area annotation. We have several ({}) url objects", urlNodes.size());
        }

        if (urlNodes.isEmpty()) {
            if (node.getArguments().size() < 2) {
                throw new InvalidAnnotationException("Invalid map area annotation (without url value)");
            }
            final String url = node.getArguments().get(1);
            return new MapUrl(url, null, false);
        }

        final Node urlNode = urlNodes.getFirst();

        int urlNodeArgumentsSize = urlNode.getArguments().size();
        if (urlNodeArgumentsSize < 2) {
            throw new InvalidAnnotationException("Invalid map area annotation (invalid url object)");
        }

        final String url = urlNode.getArguments().get(1);
        final String target = urlNodeArgumentsSize == 2 ? null : urlNode.getArguments().get(2);
        return new MapUrl(url, target, true);
    }


    private List<Node> findAnnotationNodes(TagType recordType) {
        return findNodes(nodes, recordType);
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

    static Integer parseZoomFactor(String text) {
        if (text == null
                || text.isBlank()
                || !text.matches(ZOOM_FACTOR_PATTERN)) {
            return null;
        }

        return Integer.parseInt(text.substring(1));
    }
}
