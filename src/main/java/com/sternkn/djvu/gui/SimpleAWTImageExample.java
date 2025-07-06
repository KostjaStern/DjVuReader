package com.sternkn.djvu.gui;

import javax.imageio.ImageIO;
import java.awt.Frame;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SimpleAWTImageExample extends Frame {

    /*
     Prepare the byte array:
        The byte array needs to contain the pixel data of the image. For a binary image, each bit in the byte array
        can represent a pixel (0 for black, 1 for white, or vice versa). The array length depends on the image
        dimensions (width * height / 8 for 1-bit per pixel).

     Create a DataBufferByte:
        This class wraps the byte array, allowing it to be used as a data source for image creation.

        import java.awt.image.DataBufferByte;

        // ...
        byte[] imageData = new byte[width * height / 8]; // Assuming 1-bit per pixel
        // Populate imageData with 0s and 1s representing black and white pixels
        DataBufferByte dataBuffer = new DataBufferByte(imageData, imageData.length);

     Create a SampleModel:
         This defines how pixels are stored in the DataBuffer. For a binary image, an MultiPixelPackedSampleModel is suitable.

         import java.awt.image.MultiPixelPackedSampleModel;
         import java.awt.image.DataBuffer;

         // ...
         int bitsPerPixel = 1; // For black and white image
         MultiPixelPackedSampleModel sampleModel = new MultiPixelPackedSampleModel(
              DataBuffer.TYPE_BYTE, width, height, bitsPerPixel
         );

     Create a Raster: A Raster combines the SampleModel and DataBuffer to represent the image's pixel data.

         import java.awt.image.WritableRaster;

         // ...
         WritableRaster raster = WritableRaster.createWritableRaster(sampleModel, dataBuffer, null);

     Create a ColorModel: This defines how pixel values are interpreted as colors.
        For a binary image, an IndexColorModel with a two-entry palette (black and white) is appropriate.

         import java.awt.image.IndexColorModel;

         // ...
         byte[] r = {0, (byte) 255}; // Red components for black and white
         byte[] g = {0, (byte) 255}; // Green components for black and white
         byte[] b = {0, (byte) 255}; // Blue components for black and white
         IndexColorModel colorModel = new IndexColorModel(bitsPerPixel, 2, r, g, b);

     Create a BufferedImage: Combine the ColorModel and Raster to create the BufferedImage.

         import java.awt.image.BufferedImage;

         // ...
         BufferedImage image = new BufferedImage(colorModel, raster, false, null);
     */
    private BufferedImage image;

    public SimpleAWTImageExample(String imagePath) {
        super("AWT Image Example");

        // Load the image
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Create a custom canvas to draw the image
        ImageCanvas imageCanvas = new ImageCanvas();
        add(imageCanvas);

        // Set frame properties
        setSize(image.getWidth(), image.getHeight()); // Set size to image dimensions
        setLocationRelativeTo(null); // Center the frame
        setVisible(true);

        // Add window listener to handle closing the frame
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                dispose(); // Release resources
                System.exit(0); // Exit the application
            }
        });
    }

    // Custom Canvas class to draw the image
    private class ImageCanvas extends Canvas {
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (image != null) {
                g.drawImage(image, 0, 0, this); // Draw the image at (0,0)
            }
        }
    }

    public static void main(String[] args) {
        // Replace "path/to/your/image.png" with the actual path to your image file
        new SimpleAWTImageExample("./src/main/resources/gerunds_inf.jpg");
    }
}
