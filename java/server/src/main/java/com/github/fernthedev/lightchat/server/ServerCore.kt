package com.github.fernthedev.lightchat.server;

import com.github.fernthedev.lightchat.core.Core;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

@RequiredArgsConstructor
public class ServerCore implements Core {
    @NonNull
    protected final Server server;

    @Override
    public boolean isRunning() {
        return server.isRunning();
    }

    @Override
    public Logger getLogger() {
        return server.getLogger();
    }

    @Override
    public String getName() {
        return "Server";
    }

    @Override
    public void shutdown() {
        server.shutdownServer();
    }
}
