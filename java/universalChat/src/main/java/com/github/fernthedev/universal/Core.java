package com.github.fernthedev.universal;

import org.apache.logging.log4j.Logger;

public interface Core {

    boolean isRunning();

    Logger getLogger();

    String getName();

    void runCommand(String command);

    void shutdown();
}
