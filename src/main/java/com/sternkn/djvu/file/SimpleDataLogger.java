package com.sternkn.djvu.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class SimpleDataLogger {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleDataLogger.class);

    private SimpleDataLogger() {
    }

    public static void logData(byte[] data, int capacity, String header) {
        assert capacity > 0 : "logging capacity should be positive";

        LOG.debug("------    {}    ------", header);
        final int dataSize = data.length;
        LOG.debug("dataSize = {}", dataSize);

        if (dataSize < capacity) {
            for (int ind = 0; ind < dataSize; ind++) {
                LOG.debug("data[{}] = {}", ind, data[ind]);
            }
        }
        else {
            final int logSize = capacity / 2;
            for (int ind = 0; ind < logSize; ind++) {
                LOG.debug("data[{}] = {}", ind, data[ind]);
            }

            LOG.debug(".........................");

            for (int ind = dataSize - logSize; ind < dataSize; ind++) {
                LOG.debug("data[{}] = {}", ind, data[ind]);
            }
        }
        LOG.debug("---------------------");
    }
}
