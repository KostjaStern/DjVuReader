package com.sternkn.djvu.file;

public class DjVuFileException extends RuntimeException {

    public DjVuFileException(String message) {
        super(message);
    }

    public DjVuFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
