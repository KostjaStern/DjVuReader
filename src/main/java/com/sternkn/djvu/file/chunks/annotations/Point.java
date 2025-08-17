package com.sternkn.djvu.file.chunks.annotations;

public record Point(int x, int y) {

    @Override
    public String toString() {
        return String.format("{x: %s, y: %s}", x, y);
    }
}
