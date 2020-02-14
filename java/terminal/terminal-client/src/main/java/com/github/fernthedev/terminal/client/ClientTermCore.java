package com.github.fernthedev.terminal.client;

import com.github.fernthedev.client.Client;
import com.github.fernthedev.client.ClientCore;
import com.github.fernthedev.terminal.core.TermCore;
import lombok.NonNull;

public class ClientTermCore extends ClientCore implements TermCore {
    public ClientTermCore(@NonNull Client client) {
        super(client);
    }

    @Override
    public void runCommand(String command) {
        if (client.isRegistered()) {
            ClientTerminal.sendMessage(command);
        } else {
            ClientTerminal.getLogger().error("The client has not been registered yet.");
        }
    }
}
