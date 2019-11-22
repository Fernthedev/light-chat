package com.github.fernthedev.server;

import com.github.fernthedev.core.Core;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

@RequiredArgsConstructor
public class ServerCore implements Core {
    @NonNull
    private Server server;

    @Override
    public boolean isRunning() {
        return server.isRunning();
    }

    @Override
    public Logger getLogger() {
        return Server.getLogger();
    }

    @Override
    public String getName() {
        return "Server";
    }

    @Override
    public void runCommand(String command) {
        server.dispatchCommand(command);
    }

    @Override
    public void shutdown() {
        server.shutdownServer();
    }
}
