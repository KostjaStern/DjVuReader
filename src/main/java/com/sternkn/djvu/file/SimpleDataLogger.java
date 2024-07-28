package com.sternkn.djvu.file;

public final class SimpleDataLogger {

    private SimpleDataLogger() {
    }

    public static void logData(byte[] data, int capacity, String header) {
        assert capacity > 0 : "logging capacity should be positive";

        System.out.println("------   " + header + "    ------");
        final int dataSize = data.length;
        System.out.println("dataSize = " + dataSize);

        if (dataSize < capacity) {
            for (int ind = 0; ind < dataSize; ind++) {
                System.out.println("data[" + ind + "] = " + data[ind]);
            }
        }
        else {
            final int logSize = capacity / 2;
            for (int ind = 0; ind < logSize; ind++) {
                System.out.println("data[" + ind + "] = " + data[ind]);
            }

            System.out.println(".........................");

            for (int ind = dataSize - logSize; ind < dataSize; ind++) {
                System.out.println("data[" + ind + "] = " + data[ind]);
            }
        }
        System.out.println("---------------------");
    }
}
