package com.sternkn.djvu.file.chunks.annotations;

public class InvalidAnnotationException extends RuntimeException {
    public InvalidAnnotationException(String message) {
        super(message);
    }

    public InvalidAnnotationException(String message, Throwable cause) {
        super(message, cause);
    }
}
