package com.github.fernthedev.client;

import com.github.fernthedev.universal.Core;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;

@RequiredArgsConstructor
public class ClientCore implements Core {
    @NonNull
    private Client client;

    @Override
    public boolean isRunning() {
        return client.isRunning();
    }

    @Override
    public Logger getLogger() {
        return client.getLog4jLogger();
    }

    @Override
    public String getName() {
        return "Client";
    }

    @Override
    public void runCommand(String command) {
        client.sendMessage(command);
    }

    @Override
    public void shutdown() {
        client.close();
        System.exit(0);
    }
}
