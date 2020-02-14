package com.github.fernthedev.terminal.server;


import com.github.fernthedev.core.api.APIUsage;
import com.github.fernthedev.server.*;
import com.github.fernthedev.terminal.core.packets.MessagePacket;
import com.github.fernthedev.terminal.server.backend.BannedData;
import com.github.fernthedev.terminal.server.command.Command;
import com.github.fernthedev.terminal.server.command.KickCommand;
import com.github.fernthedev.terminal.server.events.ChatEvent;
import lombok.NonNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;


public class ServerCommandHandler {

    private Server server;


    public ServerCommandHandler(Server server) {
        this.server = server;

        Server.getLogger().info("CommandHandler created");
        registerCommands();
    }

    @APIUsage
    public void dispatchCommand(@NonNull String command) {
        command = command.replaceAll(" {2}", "").trim();
        String finalCommand = command;

        if (command.equals("") || command.equals(" ")) throw new IllegalArgumentException("Command cannot be \"\"");

        new Thread(() -> {
            ChatEvent chatEvent = new ChatEvent(server.getConsole(), finalCommand, true, true);
            server.getPluginManager().callEvent(chatEvent);
            ServerTerminal.getCommandMessageParser().onCommand(chatEvent);
        }, "ConsoleChatEvent").start();
    }

    @APIUsage
    public void dispatchCommand(@NonNull SenderInterface sender, @NonNull String command) {
        command = command.replaceAll(" {2}", "").trim();
        String finalCommand = command;

        if (command.equals("") || command.equals(" ")) throw new IllegalArgumentException("Command cannot be \"\"");

        new Thread(() -> {
            ChatEvent chatEvent = new ChatEvent(sender, finalCommand, true, true);
            server.getPluginManager().callEvent(chatEvent);
            ServerTerminal.getCommandMessageParser().onCommand(chatEvent);
        }, "ConsoleChatEvent").start();
    }


    private void registerCommands() {
        ServerTerminal.registerCommand(new Command("exit") {
            @Override
            public void onCommand(SenderInterface sender, String[] args) {
                if (sender instanceof Console) {
                    ServerTerminal.sendMessage(sender, "Exiting");
                    server.shutdownServer();
                    System.exit(0);
                } else if (sender instanceof ClientPlayer) {
                    ClientPlayer clientPlayer = (ClientPlayer) sender;
                    clientPlayer.close();
                }
            }
        }).setUsage("Safely closes the server.");

        ServerTerminal.registerCommand(new Command("broadcast") {
            @Override
            public void onCommand(SenderInterface sender, String[] args) {
                if (sender instanceof Console) {
                    if (args.length > 0) {
                        StringBuilder argString = new StringBuilder();

                        int index = 0;

                        for (String arg : args) {
                            index++;

                            if (index != 1) {
                                argString.append(" ");
                            }
                            argString.append(arg);
                        }

                        String message = argString.toString();

                        ServerTerminal.broadcast("[Server] :" + message);
                    } else {
                        ServerTerminal.sendMessage(sender, "No message?");
                    }
                } else ServerTerminal.sendMessage(sender, "You don't have permission for this");
            }
        }).setUsage("Sends a broadcast message to all clients");

        ServerTerminal.registerCommand(new Command("ping") {
            @Override
            public void onCommand(SenderInterface sender, String[] args) {
                if (sender instanceof Console) {
                    ClientPlayer.pingAll();
                }

                if (sender instanceof ClientPlayer) {
                    ClientPlayer clientPlayer = (ClientPlayer) sender;
                    clientPlayer.ping();
                }
            }
        }).setUsage("Sends a ping packet to all clients");

        ServerTerminal.registerCommand(new Command("list") {
            @Override
            public void onCommand(SenderInterface sender, String[] args) {
                if (sender instanceof Console) {
                    ServerTerminal.sendMessage(sender, "Players: (" + (PlayerHandler.getUuidMap().size()) + ")");

                    for (ClientPlayer clientPlayer : new HashMap<>(PlayerHandler.getChannelMap()).values()) {
                        ServerTerminal.sendMessage(sender, clientPlayer.getName() + " :" + clientPlayer.getUuid() + " { " + clientPlayer.getAddress() + "} Ping:" + clientPlayer.getPingDelay(TimeUnit.MILLISECONDS) + "ms");
                    }
                }

                if (sender instanceof ClientPlayer) {
                    String message = "Players: (" + (PlayerHandler.getUuidMap().size() - 1) + ")";

                    for (ClientPlayer clientPlayer : new HashMap<>(PlayerHandler.getChannelMap()).values()) {
                        if (clientPlayer == null) continue;

                        message = "\n" + clientPlayer.getName() + " Ping:" + clientPlayer.getPingDelay(TimeUnit.MILLISECONDS) + "ms";

                        // ServerTerminal.sendMessage(sender, clientPlayer.getName() + " :" + clientPlayer.getId() + " Ping:" + clientPlayer.getDelayTime() + "ms");
                    }

                    ServerTerminal.sendMessage(sender, message);
                }
            }

        }).setUsage("Lists all players with ip, id and name");

        ServerTerminal.registerCommand(new KickCommand("kick"));

        ServerTerminal.registerCommand(new Command("ban") {
            @Override
            public void onCommand(SenderInterface sender, String[] args) {
                if (sender instanceof Console) {

                    if (args.length == 0) {
                        ServerTerminal.sendMessage(sender, "No player to kick or type? (ban {type} {player}) \n types: name,ip");
                    } else {
                        final String[] argName = {""};
                        Arrays.stream(args).forEachOrdered(s -> argName[0] += s);

                        for (ClientPlayer clientPlayer : new HashMap<>(PlayerHandler.getChannelMap()).values()) {
                            if (clientPlayer.getName().equals(argName[0])) {


                                StringBuilder message = new StringBuilder();

                                int index = 0;

                                for (String messageCheck : args) {
                                    index++;
                                    if (index <= 1) {
                                        message.append(messageCheck);
                                    }
                                }

                                ServerTerminal.getBanManager().addBan(clientPlayer, new BannedData(clientPlayer.getAddress()));

                                clientPlayer.sendObject(new MessagePacket("Banned: " + message));
                                clientPlayer.close();
                                break;
                            }
                        }
                    }

                } else ServerTerminal.sendMessage(sender, "You don't have permission for this");
            }
        }).setUsage("Used to ban players using id. ");

        ServerTerminal.registerCommand(new Command("help") {
            @Override
            public void onCommand(SenderInterface sender, String[] args) {
                if (args.length == 0) {
                    ServerTerminal.sendMessage(sender, "Following commands: ");
                    for (Command serverCommand : ServerTerminal.getCommands()) {
                        ServerTerminal.sendMessage(sender, serverCommand.getName());
                    }
                } else {
                    String command = args[0];
                    boolean executed = false;

                    for (Command serverCommand : ServerTerminal.getCommands()) {
                        if (serverCommand.getName().equalsIgnoreCase(command)) {
                            if (serverCommand.getUsage().equals("")) {
                                ServerTerminal.sendMessage(sender, "No usage found.");
                            } else
                                ServerTerminal.sendMessage(sender, "Usage: \n" + serverCommand.getUsage());

                            executed = true;
                            break;
                        }
                    }
                    if (!executed) ServerTerminal.sendMessage(sender, "No such command found for getting help");
                }
            }
        }).setUsage("Shows list of commands or usage of a command");


    }


}
