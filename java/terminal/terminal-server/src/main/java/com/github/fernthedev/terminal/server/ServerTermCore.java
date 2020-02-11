package com.github.fernthedev.terminal.server;

import com.github.fernthedev.server.Server;
import com.github.fernthedev.server.ServerCore;
import com.github.fernthedev.terminal.core.TermCore;
import lombok.NonNull;

public class ServerTermCore extends ServerCore implements TermCore {

    public ServerTermCore(@NonNull Server server) {
        super(server);
    }

    @Override
    public void runCommand(String command) {
        ServerTerminal.getCommandHandler().dispatchCommand(command);
    }

}
