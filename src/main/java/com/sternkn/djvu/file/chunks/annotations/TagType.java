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
