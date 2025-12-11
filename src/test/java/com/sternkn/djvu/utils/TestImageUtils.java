package com.sternkn.djvu.utils;

import com.sternkn.djvu.file.chunks.ImageRotationType;
import com.sternkn.djvu.file.coders.Pixmap;
import com.sternkn.djvu.file.coders.TestSupport;
import javafx.scene.image.Image;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.sternkn.djvu.utils.ImageUtils.composeImage;
import static com.sternkn.djvu.utils.ImageUtils.toImage;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestImageUtils extends TestSupport {

    @Test
    public void testToImageCounterClockwise90() {
        Pixmap pixmap = createPixmap("Dudaev.png");
        Image image = toImage(pixmap, ImageRotationType.COUNTER_CLOCKWISE_90);

        Pixmap actual = new PNGPixmap(image);
        Pixmap expected = createPixmap("Dudaev_COUNTER_CLOCKWISE_90.png");

        assertEquals(expected, actual);
    }

    @Test
    public void testToImageUpsideDown() {
        Pixmap pixmap = createPixmap("Dudaev.png");
        Image image = toImage(pixmap, ImageRotationType.UPSIDE_DOWN);

        Pixmap actual = new PNGPixmap(image);
        Pixmap expected = createPixmap("Dudaev_UPSIDE_DOWN.png");

        assertEquals(expected, actual);
    }

    @Test
    public void testToImageClockwise90() {
        Pixmap pixmap = createPixmap("Dudaev.png");
        Image image = toImage(pixmap, ImageRotationType.CLOCKWISE_90);

        Pixmap actual = new PNGPixmap(image);
        Pixmap expected = createPixmap("Dudaev_CLOCKWISE_90.png");

        assertEquals(expected, actual);
    }

    @Test
    public void testComposeImage() {
        PNGPixmap mask = createPixmap("Dudaev_Sjbz.png");
        PNGPixmap background = createPixmap("Dudaev_BG44.png");
        PNGPixmap foreground = createPixmap("Dudaev_FG44.png");

        Image image = composeImage(mask, background, foreground, mask.getHeight(), mask.getWidth());

        Pixmap actual = new PNGPixmap(image);
        Pixmap expected = createPixmap("Dudaev.png");

        assertEquals(expected, actual);
    }

    private PNGPixmap createPixmap(String filename) {
        return new PNGPixmap(new File("src/test/resources/test_images/" + filename));
    }
}
