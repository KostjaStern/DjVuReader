package com.sternkn.djvu.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
public class MyBenchmark {

    @Benchmark
    public void stringJoinBenchmark() {
        String str = "";
        String joinString = "hello world from xiemalin";
        for (int i = 0; i < 100; i++) {
            str += joinString;
        }
    }
}
