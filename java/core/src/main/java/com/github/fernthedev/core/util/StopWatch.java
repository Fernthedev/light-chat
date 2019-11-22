package com.github.fernthedev.core.util;

public class StopWatch {

    private volatile long start;

    public void start() {
        start = System.nanoTime();
    }

    /**
     * In MS
     * @return ms
     */
    public double now() {
        return (System.nanoTime() - start) / 1000000.0;
    }

}
