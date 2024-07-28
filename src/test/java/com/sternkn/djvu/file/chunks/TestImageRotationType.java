package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.DjVuFileException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class TestImageRotationType {

    @Test
    public void testGetRotationTypeValidFlag() {
        final byte flag = 0b0111_0101;
        ImageRotationType rotationType = ImageRotationType.getRotationType(flag);

        assertEquals(ImageRotationType.CLOCKWISE_90, rotationType);
    }

    @Test
    public void testGetRotationTypeInvalidFlag() {
        final byte flag = 0b0111_0011;
        Exception exception = assertThrows(DjVuFileException.class, () -> ImageRotationType.getRotationType(flag));

        assertEquals("Illegal flag " + flag + " in INFO chunk", exception.getMessage());
    }
}
