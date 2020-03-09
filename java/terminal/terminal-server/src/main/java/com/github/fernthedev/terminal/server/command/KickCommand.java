package com.github.fernthedev.terminal.server.command;

import com.github.fernthedev.server.*;
import com.github.fernthedev.terminal.core.packets.MessagePacket;
import com.github.fernthedev.terminal.server.ServerTerminal;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class KickCommand extends Command implements TabExecutor {

    private Server server;

    public KickCommand(@NonNull String command, Server server) {
        super(command);
        setUsage("Used to kick players using id");
        this.server = server;
    }

    @Override
    public void onCommand(SenderInterface sender, String[] args) {
        if (sender instanceof Console) {
            if (args.length == 0) {
                ServerTerminal.sendMessage(sender, "No player to kick?");
            } else {
                final String[] argName = {""};
                Arrays.stream(args).forEachOrdered(s -> argName[0] += s);

                for (ClientConnection clientConnection : new HashMap<>(server.getPlayerHandler().getChannelMap()).values()) {
                    if (argName[0].equals(clientConnection.getName())) {
                        clientConnection.sendObject(new MessagePacket("You have been kicked."));

                        clientConnection.close();
                    }

                }
            }
        } else ServerTerminal.sendMessage(sender, "You don't have permission for this");
    }

    @Override
    public List<String> getCompletions(String[] args) {

        String curArg = args[args.length - 1];
        List<ClientConnection> completions = server.getPlayerHandler().getUuidMap().values().stream().filter(
                item -> item.getName().startsWith(curArg)).collect(Collectors.toList());

        List<String> strings = new ArrayList<>();
        for (ClientConnection clientConnection : completions) {
            strings.add(clientConnection.getName());
        }

        return strings;

    }
}
