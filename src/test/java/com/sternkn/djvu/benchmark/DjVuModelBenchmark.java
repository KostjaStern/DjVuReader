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
package com.sternkn.djvu.benchmark;

import com.sternkn.djvu.benchmark.stubs.DjVuFileStub;
import com.sternkn.djvu.model.DjVuModelImpl;
import com.sternkn.djvu.model.Page;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@State(Scope.Thread)
public class DjVuModelBenchmark {
    private static final Logger LOG = LoggerFactory.getLogger(DjVuModelBenchmark.class);

    private static final Map<Long, String> PAGE_IDS = Map.of(
            1L, "0001_0001.djvu",
            2L, "0008_0001.djvu",
            3L, "0009_0001.djvu",
            4L, "0010_0001.djvu",
            6L, "Ab0005_0001.djvu",
            7L, "Ab0006_0001.djvu",
            8L, "Ab0007_0001.djvu",
            9L, "Ab0008_0001.djvu");

    @Param({"1", "2", "3", "4", "6", "7", "8", "9"})
    public long offset;

    private DjVuModelImpl model;

    @Setup(Level.Iteration)
    public void setUp() {
        LOG.info("Setting up DjVuModelImpl");
        model = new DjVuModelImpl(new DjVuFileStub());
    }

    @Benchmark
    @Fork(value = 1, warmups = 2)
    @BenchmarkMode(Mode.AverageTime)
    public void loadAsyncBenchmark() {

        Page page = new Page(offset, PAGE_IDS.get(offset));
        LOG.info("model.loadAsync for page: {}", page);

        model.loadAsync(page);
    }

    @Benchmark
    @Fork(value = 1, warmups = 2)
    @BenchmarkMode(Mode.AverageTime)
    public void loadBenchmark() {

        Page page = new Page(offset, PAGE_IDS.get(offset));
        LOG.info("model.load for page: {}", page);

        model.load(page);
    }
}
