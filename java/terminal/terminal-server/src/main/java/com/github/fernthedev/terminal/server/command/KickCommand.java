package com.github.fernthedev.terminal.server.command;

import com.github.fernthedev.server.ClientPlayer;
import com.github.fernthedev.server.Console;
import com.github.fernthedev.server.PlayerHandler;
import com.github.fernthedev.server.SenderInterface;
import com.github.fernthedev.terminal.core.packets.MessagePacket;
import com.github.fernthedev.terminal.server.ServerTerminal;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class KickCommand extends Command implements TabExecutor {
    public KickCommand(@NonNull String command) {
        super(command);
        setUsage("Used to kick players using id");
    }

    @Override
    public void onCommand(SenderInterface sender, String[] args) {
        if (sender instanceof Console) {
            if (args.length == 0) {
                ServerTerminal.sendMessage(sender, "No player to kick?");
            } else {
                for (ClientPlayer clientPlayer : new HashMap<>(PlayerHandler.socketList).values())

                    if (args[0].matches("[0-9]+")) {
                        try {
                            int id = Integer.parseInt(args[0]);
                            if (id == clientPlayer.getId()) {
                                if (args.length == 1) {
                                    clientPlayer.sendObject(new MessagePacket("You have been kicked."));
                                } else {
                                    StringBuilder message = new StringBuilder();

                                    int index = 0;

                                    for (String messageCheck : args) {
                                        index++;
                                        if (index <= 1) {
                                            message.append(messageCheck);
                                        }
                                    }

                                    clientPlayer.sendObject(new MessagePacket("Kicked: " + message));
                                }
                                clientPlayer.close();
                            }
                        } catch (NumberFormatException e) {
                            ServerTerminal.sendMessage(sender, "Not able to parse number.");
                        }
                    }
            }
        }else ServerTerminal.sendMessage(sender, "You don't have permission for this");
    }

    @Override
    public List<String> getCompletions(String[] args) {

        String curArg = args[args.length - 1];
        List<ClientPlayer> completions = PlayerHandler.players.values().stream().filter(
                item -> item.getName().startsWith(curArg)).collect(Collectors.toList());

        List<String> strings = new ArrayList<>();
        for (ClientPlayer clientPlayer : completions) {
            strings.add(clientPlayer.getName());
        }

        return strings;

    }
}
