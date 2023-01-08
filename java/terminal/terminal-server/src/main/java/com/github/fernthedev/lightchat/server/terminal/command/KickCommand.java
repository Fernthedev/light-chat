package com.github.fernthedev.lightchat.server.terminal.command;

import com.github.fernthedev.lightchat.core.encryption.PacketTransporter;
import com.github.fernthedev.lightchat.server.ClientConnection;
import com.github.fernthedev.lightchat.server.Console;
import com.github.fernthedev.lightchat.server.SenderInterface;
import com.github.fernthedev.lightchat.server.Server;
import com.github.fernthedev.lightchat.server.terminal.ServerTerminal;
import com.github.fernthedev.terminal.core.packets.MessagePacket;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;

public class KickCommand extends Command implements TabExecutor {

    private final Server server;

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
                        clientConnection.sendObject(new PacketTransporter(new MessagePacket("You have been kicked."), true));

                        clientConnection.close();
                    }

                }
            }
        } else ServerTerminal.sendMessage(sender, "You don't have permission for this");
    }

    @Override
    public List<String> getCompletions(SenderInterface senderInterface, LinkedList<String> args) {

        String curArg = args.getLast();
        List<ClientConnection> completions = server.getPlayerHandler().getUuidMap().values().stream().filter(
                item -> item.getName().startsWith(curArg)).collect(Collectors.toList());

        List<String> strings = new ArrayList<>();
        for (ClientConnection clientConnection : completions) {
            strings.add(clientConnection.getName());
        }

        return strings;

    }
}
