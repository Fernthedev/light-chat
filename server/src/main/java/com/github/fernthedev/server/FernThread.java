package com.github.fernthedev.server;

public class FernThread extends Thread {

    boolean running;

    public FernThread(Runnable runnable) {
        super(runnable);
    }

    public FernThread() {}

    public boolean isRunning() {
        return running;
    }


    public void startThread() {
        running = true;
        start();
    }

}
