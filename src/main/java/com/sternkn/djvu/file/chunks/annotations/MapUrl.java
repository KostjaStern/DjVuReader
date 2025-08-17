package com.sternkn.djvu.file.chunks.annotations;

public record MapUrl(String url,  String target, boolean isObject) {

    @Override
    public String toString() {
        return String.format("{url: %s, target: %s, isObject: %s}", url, target, isObject);
    }
}
