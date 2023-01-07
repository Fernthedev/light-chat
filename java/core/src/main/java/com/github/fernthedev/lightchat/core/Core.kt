package com.github.fernthedev.lightchat.core;


import org.slf4j.Logger;

public interface Core {

    boolean isRunning();

    Logger getLogger();

    String getName();



    void shutdown();
}
