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

public enum AnnotationType {
    // 8.3.4.1 Initial Document View
    BACKGROUND_COLOR("background"),
    INITIAL_ZOOM("zoom"),
    INITIAL_DISPLAY_LEVEL("mode"),
    ALIGNMENT("align"),

    // 8.3.4.2 Maparea (overprinted annotations)
    MAP_AREA("maparea"),

    // 8.3.4.3 Printed headers and footers
    PRINTED_HEADER("phead"), // (phead "left::Sept 20, 2005" "right::Today’s Menu " )
    PRINTED_FOOTER("pfoot"); // (pfoot "center::Chez Dominique" )

    private final String token;

    AnnotationType(String token) {
        this.token = token;
    }

    public static AnnotationType fromToken(String token) {
        for (AnnotationType type : AnnotationType.values()) {
            if (type.token.equals(token)) {
                return type;
            }
        }
        return null;
    }

    public String getToken() {
        return token;
    }
}
