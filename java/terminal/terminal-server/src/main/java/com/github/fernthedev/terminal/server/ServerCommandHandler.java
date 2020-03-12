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
                } else if (sender instanceof ClientConnection) {
                    ClientConnection clientConnection = (ClientConnection) sender;
                    clientConnection.close();
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
                    server.getPlayerHandler().getChannelMap().forEach((channel, connection) -> connection.ping());
                }

                if (sender instanceof ClientConnection) {
                    ClientConnection clientConnection = (ClientConnection) sender;
                    clientConnection.ping();
                }
            }
        }).setUsage("Sends a ping packet to all clients");

        ServerTerminal.registerCommand(new Command("list") {
            @Override
            public void onCommand(SenderInterface sender, String[] args) {
                if (sender instanceof Console) {
                    ServerTerminal.sendMessage(sender, "Players: (" + (server.getPlayerHandler().getUuidMap().size()) + ")");

                    for (ClientConnection clientConnection : new HashMap<>(server.getPlayerHandler().getChannelMap()).values()) {
                        ServerTerminal.sendMessage(sender, clientConnection.getName() + " :" + clientConnection.getUuid() + " { " + clientConnection.getAddress() + "} Ping:" + clientConnection.getPingDelay(TimeUnit.MILLISECONDS) + "ms");
                    }
                }

                if (sender instanceof ClientConnection) {
                    String message = "Players: (" + (server.getPlayerHandler().getUuidMap().size() - 1) + ")";

                    for (ClientConnection clientConnection : new HashMap<>(server.getPlayerHandler().getChannelMap()).values()) {
                        if (clientConnection == null) continue;

                        message = "\n" + clientConnection.getName() + " Ping:" + clientConnection.getPingDelay(TimeUnit.MILLISECONDS) + "ms";

                        // ServerTerminal.sendMessage(sender, clientConnection.getName() + " :" + clientConnection.getId() + " Ping:" + clientConnection.getDelayTime() + "ms");
                    }

                    ServerTerminal.sendMessage(sender, message);
                }
            }

        }).setUsage("Lists all players with ip, id and name");

        ServerTerminal.registerCommand(new KickCommand("kick", server));

        ServerTerminal.registerCommand(new Command("ban") {
            @Override
            public void onCommand(SenderInterface sender, String[] args) {
                if (sender instanceof Console) {

                    if (args.length == 0) {
                        ServerTerminal.sendMessage(sender, "No player to kick or type? (ban {type} {player}) \n types: name,ip");
                    } else {
                        final String[] argName = {""};
                        Arrays.stream(args).forEachOrdered(s -> argName[0] += s);

                        for (ClientConnection clientConnection : new HashMap<>(server.getPlayerHandler().getChannelMap()).values()) {
                            if (clientConnection.getName().equals(argName[0])) {


                                StringBuilder message = new StringBuilder();

                                int index = 0;

                                for (String messageCheck : args) {
                                    index++;
                                    if (index <= 1) {
                                        message.append(messageCheck);
                                    }
                                }

                                ServerTerminal.getBanManager().addBan(clientConnection, new BannedData(clientConnection.getAddress()));

                                clientConnection.sendObject(new MessagePacket("Banned: " + message));
                                clientConnection.close();
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