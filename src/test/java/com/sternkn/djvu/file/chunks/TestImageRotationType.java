package com.sternkn.djvu.file.chunks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestImageRotationType {

    @Test
    public void testGetRotationTypeValidFlag() {
        final byte flag = 0b0111_0101;
        ImageRotationType rotationType = ImageRotationType.getRotationType(flag);

        assertEquals(ImageRotationType.CLOCKWISE_90, rotationType);
    }

    @Test
    public void testGetRotationTypeForUnknownFlag() {
        assertEquals(ImageRotationType.NO_ROTATION, ImageRotationType.getRotationType(0));
    }
}
