package com.sternkn.djvu.file.chunks.annotations;

public enum RecordType {

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

    RecordType(String token) {
        this.token = token;
    }

    public static RecordType fromToken(String token) {
        for (RecordType type : RecordType.values()) {
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
