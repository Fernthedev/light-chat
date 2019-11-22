package com.github.fernthedev.core;


import org.slf4j.Logger;

public interface Core {

    boolean isRunning();

    Logger getLogger();

    String getName();

    void runCommand(String command);

    void shutdown();
}
