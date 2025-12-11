package com.sternkn.djvu.utils;

import com.sternkn.djvu.file.coders.Pixmap;
import com.sternkn.djvu.file.coders.TestSupport;
import javafx.scene.image.Image;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.sternkn.djvu.utils.ImageUtils.composeImage;

public class TestImageUtils extends TestSupport {

    @Test
    public void testComposeImage() {
        PNGPixmap mask = createPixmap("Dudaev_Sjbz.png");
        PNGPixmap background = createPixmap("Dudaev_BG44.png");
        PNGPixmap foreground = createPixmap("Dudaev_FG44.png");

        Image image = composeImage(mask, background, foreground, mask.getHeight(), mask.getWidth());

        Pixmap actual = new PNGPixmap(image);
        Pixmap expected = createPixmap("Dudaev.png");

        assertPixmapEquals(expected, actual);
    }

    private PNGPixmap createPixmap(String filename) {
        return new PNGPixmap(new File("src/test/resources/test_images/" + filename));
    }
}
