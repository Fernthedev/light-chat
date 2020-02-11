package com.github.fernthedev.client;

import com.github.fernthedev.core.Core;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;


@RequiredArgsConstructor
public class ClientCore implements Core {
    @NonNull
    protected Client client;

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
    public void shutdown() {
        client.close();
        System.exit(0);
    }
}
