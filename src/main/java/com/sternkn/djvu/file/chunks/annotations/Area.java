package com.sternkn.djvu.file.chunks.annotations;

public abstract class Area {
    protected AreaType type;
    protected Border border;

    protected Area(AreaType type) {
        this.type = type;
    }

    public Border getBorder() {
        return border;
    }

    public AreaType getType() {
        return type;
    }
}
