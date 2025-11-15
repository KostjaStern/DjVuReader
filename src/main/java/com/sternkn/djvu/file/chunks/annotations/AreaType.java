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

public enum AreaType {

    RECTANGLE("rect"),   // (rect xmin ymin width height)
    OVAL("oval"),        // (oval xmin ymin width height)
    TEXT_BOX("text"),    // (text xmin ymin width height)
    POLYGON("poly"),     // (poly x0 y0 x1 y1 ... )
    LINE("line");        // (line x0 y0 x1 y1)

    private final String token;

    AreaType(String token) {
        this.token = token;
    }

    public static AreaType fromToken(String token) {
        for (AreaType type : values()) {
            if (type.token.equals(token)) {
                return type;
            }
        }
        return null;
    }

    public static AreaType fromToken(Node node) {
        for (Node child : node.getChildren()) {
            if (child.getArguments().isEmpty()) {
                continue;
            }
            AreaType type = fromToken(child.getArguments().getFirst());
            if (type != null) {
                return type;
            }
        }

        return null;
    }
}
