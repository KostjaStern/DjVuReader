/*
    Copyright (C) 2025 Kostya Stern

    This program is free software; you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation; either version 2 of the License, or (at your option)
    any later version.

    This program is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
    more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc., 51
    Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
*/
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
