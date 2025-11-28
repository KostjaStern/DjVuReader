package com.sternkn.djvu.utils.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class ExceptionUtils {

    private ExceptionUtils() {
    }

    public static String getStackTraceAsString(Throwable t) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            t.printStackTrace(pw);
            return sw.toString();
        }
    }
}
