package com.github.fernthedev.server.backend.auth;

import java.util.TimerTask;

public class ThreadTask extends TimerTask {
    Thread myThreadObj;
    ThreadTask(Thread t){
        this.myThreadObj=t;
    }
    public void run() {
        myThreadObj.start();
    }
}
