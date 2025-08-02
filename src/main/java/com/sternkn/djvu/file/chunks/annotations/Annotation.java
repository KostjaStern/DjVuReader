package com.sternkn.djvu.file.chunks.annotations;

public abstract class Annotation {

    protected final AnnotationType type;

    public Annotation(AnnotationType type) {
        this.type = type;
    }

    public AnnotationType getType() {
        return type;
    }
}
