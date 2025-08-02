package com.sternkn.djvu.file.chunks.annotations;

/**
 *  8.3.4.1.4 Alignment
 *
 *  (align horzalign vertalign)
 */
public class Alignment extends Annotation {
    private final AlignmentType horizontal; // left, center, or right
    private final AlignmentType vertical;   // top, center, or bottom

    public Alignment(AlignmentType horizontal, AlignmentType vertical) {
        super(AnnotationType.ALIGNMENT);
        this.horizontal = horizontal;
        this.vertical = vertical;

        validateFields();
    }

    private void validateFields() {
        if (this.horizontal == null || this.horizontal == AlignmentType.TOP || this.horizontal == AlignmentType.BOTTOM) {
            throw new InvalidAnnotationException("Invalid horizontal value: " + this.horizontal);
        }
        if (this.vertical == null || this.vertical == AlignmentType.LEFT || this.vertical == AlignmentType.RIGHT) {
            throw new InvalidAnnotationException("Invalid vertical value: " + this.vertical);
        }
    }

    public AlignmentType getHorizontal() {
        return horizontal;
    }

    public AlignmentType getVertical() {
        return vertical;
    }
}
