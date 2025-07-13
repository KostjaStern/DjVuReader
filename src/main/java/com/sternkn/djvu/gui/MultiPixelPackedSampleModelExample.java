package com.sternkn.djvu.gui;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class MultiPixelPackedSampleModelExample {

    public static void main(String[] args) {
        int width = 7;  // 8 pixels wide
        int height = 5; // 1 pixel high (for simplicity)
        int bitsPerPixel = 1; // 1 bit per pixel (e.g., for binary images)

        // Create a MultiPixelPackedSampleModel for 1-bit pixels packed into bytes
        // dataType: DataBuffer.TYPE_BYTE
        // w: width of the image
        // h: height of the image
        // numberOfBits: bits per pixel
        // scanlineStride: number of data elements per scanline (calculated based on packing)
        // dataBitOffset: bit offset to the first pixel in the data buffer
        MultiPixelPackedSampleModel mppsm = new MultiPixelPackedSampleModel(
                DataBuffer.TYPE_BYTE,
                width,
                height,
                bitsPerPixel);

        // Create a DataBufferByte to hold the pixel data
        // The size of the data buffer depends on the scanline stride and height
        // For a 1-bit image 8 pixels wide, 1 byte is needed per scanline (8 bits per byte)
        System.out.println("mppsm.getScanlineStride() = " + mppsm.getScanlineStride());

        byte[] data = new byte[mppsm.getScanlineStride() * height];
        System.out.println("data.length = " + data.length);

        DataBufferByte dataBuffer = new DataBufferByte(data, data.length);

        // Create a WritableRaster from the SampleModel and DataBuffer
        WritableRaster raster = Raster.createWritableRaster(mppsm, dataBuffer, null);

        // Set some pixel values
        // For 1-bit pixels, values are 0 or 1
        raster.setSample(0, 0, 0, 1); // Set pixel (0,0) to 1
        raster.setSample(0, 4, 0, 1); // Set pixel (0,4) to 1
        raster.setSample(0, 2, 0, 1); // Set pixel (0,2) to 1
        raster.setSample(2, 0, 0, 1); // Set pixel (2,0) to 1
        raster.setSample(2, 1, 0, 1); // Set pixel (2,1) to 1
        raster.setSample(5, 0, 0, 1); // Set pixel (5,0) to 1
        raster.setSample(6, 4, 0, 1); // Set pixel (6,4) to 1

        // Get and print pixel values
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                System.out.println("Pixel (" + x + "," + y + "): " + raster.getSample(x, y, 0));
            }
        }

//        System.out.println("Pixel (0,0): " + raster.getSample(0, 0, 0));
//        System.out.println("Pixel (0,2): " + raster.getSample(0, 2, 0));
//        System.out.println("Pixel (1,0): " + raster.getSample(1, 0, 0));
//        System.out.println("Pixel (2,0): " + raster.getSample(2, 0, 0));
//        System.out.println("Pixel (7,0): " + raster.getSample(7, 0, 0));

        // You can also inspect the raw data in the DataBuffer
        StringBuilder output = new StringBuilder("Raw data in DataBuffer: ");
        for (int ind = 0; ind < data.length; ind++) {
            output.append(String.format("%02X ", data[ind] & 0xFF));
        }

        System.out.println(output);
        // Expected output for data[0]:
        // Pixel (0,0) is bit 7 (MSB), Pixel (7,0) is bit 0 (LSB)
        // If (0,0), (2,0), (7,0) are set to 1, then data[0] would be 10100001 (binary) = A1 (hex)
    }
}
