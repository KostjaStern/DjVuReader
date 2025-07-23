package com.sternkn.djvu.file.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestStringUtils {

    @Test
    public void testPadRight() {
        assertEquals("12        ", StringUtils.padRight(12, 10));
    }

    @Test
    public void testPadLeft() {
        assertEquals("        12", StringUtils.padLeft(12, 10));
    }
}
