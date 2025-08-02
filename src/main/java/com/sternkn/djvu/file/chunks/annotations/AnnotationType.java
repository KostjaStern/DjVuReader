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
