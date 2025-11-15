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

    @Override
    public String toString() {
        return String.format("{horizontal: %s, vertical: %s}", horizontal, vertical);
    }
}
