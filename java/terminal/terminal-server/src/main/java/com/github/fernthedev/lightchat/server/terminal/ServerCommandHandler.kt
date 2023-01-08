package com.github.fernthedev.lightchat.server.terminal;


import com.github.fernthedev.lightchat.core.ColorCode;
import com.github.fernthedev.lightchat.core.StaticHandler;
import com.github.fernthedev.lightchat.core.api.APIUsage;
import com.github.fernthedev.lightchat.core.encryption.PacketTransporter;
import com.github.fernthedev.lightchat.core.packets.IllegalConnectionPacket;
import com.github.fernthedev.lightchat.core.packets.SelfMessagePacket;
import com.github.fernthedev.lightchat.core.packets.handshake.ConnectedPacket;
import com.github.fernthedev.lightchat.core.packets.handshake.InitialHandshakePacket;
import com.github.fernthedev.lightchat.core.packets.handshake.KeyResponsePacket;
import com.github.fernthedev.lightchat.core.packets.handshake.RequestConnectInfoPacket;
import com.github.fernthedev.lightchat.core.packets.latency.PingPacket;
import com.github.fernthedev.lightchat.core.packets.latency.PingReceive;
import com.github.fernthedev.lightchat.core.packets.latency.PongPacket;
import com.github.fernthedev.lightchat.server.*;
import com.github.fernthedev.lightchat.server.terminal.command.Command;
import com.github.fernthedev.lightchat.server.terminal.command.KickCommand;
import com.github.fernthedev.lightchat.server.terminal.events.ChatEvent;
import com.github.fernthedev.terminal.core.packets.MessagePacket;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class ServerCommandHandler {

    private final Server server;


    public ServerCommandHandler(Server server) {
        this.server = server;

        server.getLogger().info("CommandHandler created");
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

        ServerTerminal.registerCommand(new Command("stop") {
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

                        ServerTerminal.broadcast("[Server]: " + message);
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
                        ServerTerminal.sendMessage(sender, clientConnection.getName() + " :" + clientConnection.getUuid() + " {" + clientConnection.getAddress() + "} [" + clientConnection.getOs() + "/" + clientConnection.getLangFramework() + "] Ping:" + clientConnection.getPingDelay(TimeUnit.MILLISECONDS) + "ms");
                    }
                }

                if (sender instanceof ClientConnection) {
                    String message = "Players: (" + (server.getPlayerHandler().getUuidMap().size() - 1) + ")";

                    for (ClientConnection clientConnection : new HashMap<>(server.getPlayerHandler().getChannelMap()).values()) {
                        if (clientConnection == null) continue;

                        message = "\n" + clientConnection.getName() + "[" + clientConnection.getOs() + "/" + clientConnection.getLangFramework() + "] Ping:" + clientConnection.getPingDelay(TimeUnit.MILLISECONDS) + "ms";

                        // ServerTerminal.sendMessage(sender, clientConnection.getName() + " :" + clientConnection.getId() + " Ping:" + clientConnection.getDelayTime() + "ms");
                    }

                    ServerTerminal.sendMessage(sender, message);
                }
            }

        }).setUsage("Lists all players with ip, id and name");

        if (StaticHandler.isDebug()) {
            ServerTerminal.registerCommand(new Command("testpackets") {
                @SneakyThrows
                @Override
                public void onCommand(SenderInterface sender, String[] args) {
                    List<ClientConnection> connections = new ArrayList<>();
                    if (sender instanceof Console) {
                        connections.addAll(server.getPlayerHandler().getChannelMap().values());
                    }

                    if (sender instanceof ClientConnection) {
                        connections.add((ClientConnection) sender);
                    }

                    for (ClientConnection connection : connections) {
                        ServerTerminal.sendMessage(sender, ColorCode.YELLOW + "Running packets on " + connection.getName());
                        connection.sendPacket(new MessagePacket("test"));

                        connection.sendPacket(new PingPacket());
                        connection.sendPacket(new PingReceive());
                        connection.sendPacket(new PongPacket());


                        if (connection.getTempKeyPair() != null) {
                            connection.sendPacket(new InitialHandshakePacket(connection.getTempKeyPair().getPublic(), connection.getVersionData()));
                            connection.sendPacket(new KeyResponsePacket(connection.getSecretKey(), connection.getTempKeyPair().getPublic()));
                        }

                        connection.sendPacket(new RequestConnectInfoPacket());
                        connection.sendPacket(new ConnectedPacket(connection.getName(), connection.getOs(), connection.getVersionData(), connection.getLangFramework()));


                        for (SelfMessagePacket.MessageType messageType : SelfMessagePacket.MessageType.values()) {
                            connection.sendPacket(new SelfMessagePacket(messageType));
                        }
                        connection.sendPacket(new IllegalConnectionPacket("test packet"));

                    }
                }
            });
        }

        ServerTerminal.registerCommand(new KickCommand("kick", server));

        ServerTerminal.registerCommand(new Command("ban") {
            @Override
            public void onCommand(SenderInterface sender, String[] args) {
                if (sender instanceof Console) {

                    if (args.length <= 1) {
                        ServerTerminal.sendMessage(sender, "No player to kick or type? (ban {type} {player}) \n types: name,ip");
                    } else {

                        StringBuilder message = new StringBuilder();

                        int index = 0;

                        for (String messageCheck : args) {
                            index++;
                            if (index >= 2) {
                                message.append(messageCheck);
                            }
                        }

                        switch (args[0].toLowerCase()) {
                            case "name":
                                for (ClientConnection clientConnection : new HashMap<>(server.getPlayerHandler().getChannelMap()).values()) {
                                    if (clientConnection.getName().equals(args[1])) {
                                        clientConnection.sendObject(new PacketTransporter(new MessagePacket("Banned: " + message), true));
                                        clientConnection.close();

                                        server.getBanManager().ban(clientConnection.getAddress());

                                        break;
                                    }
                                }
                                break;

                            case "ip":
                                for (ClientConnection clientConnection : new HashMap<>(server.getPlayerHandler().getChannelMap()).values()) {
                                    if (clientConnection.getAddress().equals(args[1])) {
                                        clientConnection.sendObject(new PacketTransporter(new MessagePacket("Banned: " + message), true));
                                        clientConnection.close();
                                    }
                                }

                                server.getBanManager().ban(args[1]);
                                break;
                            default:
                                ServerTerminal.sendMessage(sender, "Unknown argument " + args[0]);
                                return;
                        }

                        ServerTerminal.sendMessage(sender, ColorCode.GREEN + "Banned " + args[1]);
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
