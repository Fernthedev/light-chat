package com.github.fernthedev.core.util;

public class ThreadUtil {

    private ThreadUtil() {}

    public static Thread runAsync(Runnable r) {
        Thread thread = new Thread(r);
        thread.start();
        return thread;
    }

    public static Thread runAsync(String name, Runnable r) {
        Thread thread = new Thread(r, name);
        thread.start();
        return thread;
    }

}
