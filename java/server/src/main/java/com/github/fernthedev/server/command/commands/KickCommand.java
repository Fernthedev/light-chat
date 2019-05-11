package com.github.fernthedev.server.command.commands;

import com.github.fernthedev.packets.MessagePacket;
import com.github.fernthedev.server.ClientPlayer;
import com.github.fernthedev.server.Console;
import com.github.fernthedev.server.PlayerHandler;
import com.github.fernthedev.server.Server;
import com.github.fernthedev.server.command.Command;
import com.github.fernthedev.server.command.CommandSender;
import com.github.fernthedev.server.command.TabExecutor;
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
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof Console) {
            if (args.length == 0) {
                sender.sendMessage("No player to kick?");
            } else {
                for (ClientPlayer clientPlayer : new HashMap<>(Server.socketList).values())

                    if (args[0].matches("[0-9]+")) {
                        try {
                            int id = Integer.parseInt(args[0]);
                            if (id == clientPlayer.getId()) {
                                if (args.length == 1) {
                                    clientPlayer.sendObject(MessagePacket.newBuilder().setMessage("You have been kicked.").setCommand(false).build());
                                } else {
                                    StringBuilder message = new StringBuilder();

                                    int index = 0;

                                    for (String messageCheck : args) {
                                        index++;
                                        if (index <= 1) {
                                            message.append(messageCheck);
                                        }
                                    }

                                    clientPlayer.sendObject(MessagePacket.newBuilder().setMessage("Kicked: " + message).setCommand(false).build());
                                }
                                clientPlayer.close();
                            }
                        } catch (NumberFormatException e) {
                            sender.sendMessage("Not able to parse number.");
                        }
                    }
            }
        }else sender.sendMessage("You don't have permission for this");
    }

    @Override
    public List<String> getCompletions(String[] args) {
        if(args.length == 1) {
            String curArg = args[args.length - 1];

            List<ClientPlayer> completions = PlayerHandler.players.values().stream().filter(
                    item -> item.getName().startsWith(curArg)).collect(Collectors.toList());

            List<String> strings = new ArrayList<>();
            for (ClientPlayer clientPlayer : completions) {
                strings.add(clientPlayer.getName());
            }

            return strings;
        }

        return new ArrayList<>();

    }
}
