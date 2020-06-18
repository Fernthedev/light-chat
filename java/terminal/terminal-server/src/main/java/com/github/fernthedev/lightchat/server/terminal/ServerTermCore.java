package com.github.fernthedev.lightchat.server.terminal;

import com.github.fernthedev.lightchat.server.Server;
import com.github.fernthedev.lightchat.server.ServerCore;
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
