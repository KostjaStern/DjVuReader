package com.sternkn.djvu.file.chunks.annotations;

import com.sternkn.djvu.file.chunks.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Predicate;

import static com.sternkn.djvu.file.utils.NumberUtils.hexToInt;
import static com.sternkn.djvu.file.utils.NumberUtils.toInt;

public class ParserUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ParserUtils.class);

    private static final String COLOR_PATTERN = "^#[0-9a-fA-F]{6}$";

    public static List<Node> findNodes(List<Node> nodes, TagType tagType) {
        final String bgToken = tagType.getToken();
        return nodes.stream()
                .filter(n -> !n.getArguments().isEmpty())
                .filter(n -> bgToken.equals(n.getArguments().getFirst()))
                .toList();
    }

    /**
     *
     * @param node - parent node
     * @param tagType - tags like (opacity op) , (shadow_* thickness) , (width w)
     * @param validator - additional condition that should pass the returned integer value
     * @param defaultValue - default value
     * @return integer value for the specified tagType
     */
    public static Integer parseIntNode(Node node, TagType  tagType, Predicate<Integer> validator, Integer defaultValue) {
        List<Node> nodes = findNodes(node.getChildren(), tagType);
        if (nodes.isEmpty()) {
            return defaultValue;
        }

        if (nodes.size() > 1) {
            LOG.warn("We have more than one tags with type = {}", tagType);
        }

        Node parseNode = nodes.getFirst();
        int argSize = parseNode.getArguments().size();
        if (argSize < 2) {
            throw new InvalidAnnotationException("We have the invalid number of arguments: "
                    + argSize + " for tag type " + tagType);
        }

        int value = toInt(parseNode.getArguments().get(1));

        if (validator != null && !validator.test(value)) {
            throw new InvalidAnnotationException("We have the invalid value: " + value + " for tag type " + tagType);
        }

        return value;
    }

    public static Integer parseIntNode(Node node, TagType  tagType, Predicate<Integer> validator) {
        return parseIntNode(node, tagType, validator, null);
    }

    /**
     * @param nodes - nodes for search
     * @param tagType - one of the following types: BACK_COLOR, BACKGROUND_COLOR, BORDER, HIGHLIGHT,
     *                     LINE_COLOR, TEXT_COLOR
     * @return color
     */
    public static Color parseColorNode(List<Node> nodes, TagType tagType) {
        List<Node> nds = findNodes(nodes, tagType);
        if (nds.isEmpty()) {
            return null;
        }

        if (nds.size() > 1) {
            LOG.warn("More than one node found for record type {}. We will take into account the first one", tagType);
        }

        Node node = nds.getFirst();

        if (node.getArguments().size() < 2) {
            throw new InvalidAnnotationException("Node " + tagType + " must have at least one argument");
        }

        String textColor = node.getArguments().get(1);
        return parseColor(textColor);
    }

    /**
     *
     * @param node - parent node
     * @param recordType - one of the following types: PUSH_PIN, NO_BORDER, XOR, BORDER_AVIS
     * @return true if node with this type is present
     */
    public static boolean isTrue(Node node, TagType recordType) {
        List<Node> nodes = findNodes(node.getChildren(), recordType);
        return !nodes.isEmpty();
    }

    /**
     *   text - coded color in this format: #RRGGBB
     */
    public static Color parseColor(String text) {
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
}
