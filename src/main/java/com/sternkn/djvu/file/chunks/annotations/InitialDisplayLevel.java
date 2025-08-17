package com.sternkn.djvu.file.chunks.annotations;

/**
 *  8.3.4.1.3 Initial Display level
 *
 *  (mode modevalue)
 */
public class InitialDisplayLevel extends Annotation {
    private final ModeType modeType;

    public InitialDisplayLevel(ModeType modeType) {
        super(AnnotationType.INITIAL_DISPLAY_LEVEL);
        this.modeType = modeType;
    }

    public ModeType getModeType() {
        return modeType;
    }

    @Override
    public String toString() {
        return String.format("{modeType: %s}", modeType);
    }
}
