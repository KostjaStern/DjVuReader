package com.sternkn.djvu.utils;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.sternkn.djvu.utils.ExceptionUtils.getStackTraceAsString;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestExceptionUtils {

    @Test
    public void testGetStackTraceAsString() {
        RuntimeException exception = new RuntimeException("Runtime exception wrapper",
                new IOException("File test_file.log not found"));

        String stackTrace = getStackTraceAsString(exception);
        assertTrue(stackTrace.contains("java.lang.RuntimeException: Runtime exception wrapper"));
        assertTrue(stackTrace.contains("Caused by: java.io.IOException: File test_file.log not found"));
    }
}
