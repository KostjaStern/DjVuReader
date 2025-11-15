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

public enum TagType {

    BACKGROUND_COLOR("background"),
    INITIAL_ZOOM("zoom"),
    INITIAL_DISPLAY_LEVEL("mode"),
    ALIGNMENT("align"),
    MAP_AREA("maparea"),
    PRINTED_HEADER("phead"),
    PRINTED_FOOTER("pfoot"),

    URL("url"),

    RECTANGLE("rect"),
    OVAL("oval"),
    TEXT_BOX("text"),
    POLYGON("poly"),
    LINE("line"),

    NO_BORDER("none"),
    XOR("xor"),
    BORDER("border"),
    SHADOW_IN("shadow_in"),
    SHADOW_OUT("shadow_out"),
    SHADOW_EIN("shadow_ein"),
    SHADOW_EOUT("shadow_eout"),

    BORDER_AVIS("border_avis"),

    HIGHLIGHT("hilite"),
    OPACITY("opacity"),

    ARROW("arrow"),
    WIDTH("width"),
    LINE_COLOR("lineclr"),
    BACK_COLOR("backclr"),
    TEXT_COLOR("textclr"),
    PUSH_PIN("pushpin");

    private final String token;

    TagType(String token) {
        this.token = token;
    }

    public static TagType fromToken(String token) {
        for (TagType type : TagType.values()) {
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
