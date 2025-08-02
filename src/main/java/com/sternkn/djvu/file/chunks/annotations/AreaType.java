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
        for (AreaType type : AreaType.values()) {
            if (type.token.equals(token)) {
                return type;
            }
        }
        return null;
    }
}
