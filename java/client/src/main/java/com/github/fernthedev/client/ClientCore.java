package com.github.fernthedev.client;

import com.github.fernthedev.core.Core;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;


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
        return client.getLogger();
    }

    @Override
    public String getName() {
        return "Client";
    }

    @Override
    public void runCommand(String command) {
        if (client.registered) {
            client.sendMessage(command);
        } else {
            getLogger().error("The client has not been registered yet.");
        }
    }

    @Override
    public void shutdown() {
        client.close();
        System.exit(0);
    }
}
