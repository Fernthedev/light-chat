package com.github.fernthedev.server;


import com.github.fernthedev.packets.MessagePacket;
import com.github.fernthedev.server.backend.BannedData;
import com.github.fernthedev.server.backend.CommandMessageParser;
import com.github.fernthedev.server.command.Command;
import com.github.fernthedev.server.command.CommandSender;
import com.github.fernthedev.server.command.KickCommand;
import com.github.fernthedev.server.event.chat.ChatEvent;
import com.github.fernthedev.universal.StaticHandler;
import lombok.NonNull;
import org.jline.reader.UserInterruptException;

import java.util.HashMap;

import static com.github.fernthedev.server.CommandWorkerThread.commandList;


public class ServerCommandHandler implements Runnable {

    private Server server;


    ServerCommandHandler(Server server) {
        this.server = server;

        Server.getLogger().info("CommandHandler created");
        registerCommands();
    }


    @Deprecated
    public void run() {
        try {
            Server.getLogger().info("Type Command: (try help)");
            while (server.isRunning()) {

                String command = StaticHandler.readLine("> ");

                command = command.replaceAll(" {2}", "").trim();

                if (command.equals("") || command.equals(" ")) continue;

                dispatchCommand(command);
            }
        } catch (UserInterruptException e) {
            server.shutdownServer();
        }
    }

    public void dispatchCommand(@NonNull String command) {
        command = command.replaceAll(" {2}", "").trim();
        String finalCommand = command;

        if (command.equals("") || command.equals(" ")) throw new IllegalArgumentException("Command cannot be \"\"");

        new Thread(() -> {
            ChatEvent chatEvent = new ChatEvent(server.getConsole(), finalCommand,true,true);
            server.getPluginManager().callEvent(chatEvent);
            CommandMessageParser.onCommand(chatEvent);
        }, "ConsoleChatEvent").start();
    }

    public void dispatchCommand(@NonNull CommandSender sender, @NonNull String command) {
        command = command.replaceAll(" {2}", "").trim();
        String finalCommand = command;

        if (command.equals("") || command.equals(" ")) throw new IllegalArgumentException("Command cannot be \"\"");

        new Thread(() -> {
            ChatEvent chatEvent = new ChatEvent(sender, finalCommand,true,true);
            server.getPluginManager().callEvent(chatEvent);
            CommandMessageParser.onCommand(chatEvent);
        }, "ConsoleChatEvent").start();
    }


    private void registerCommands() {
        server.registerCommand(new Command("exit") {
            @Override
            public void onCommand(CommandSender sender, String[] args) {
                if(sender instanceof Console) {
                    sender.sendMessage("Exiting");
                    server.shutdownServer();
                    System.exit(0);
                }else if(sender instanceof ClientPlayer) {
                    ClientPlayer clientPlayer = (ClientPlayer) sender;
                    clientPlayer.close();
                }
            }
        }).setUsage("Safely closes the server.");

        server.registerCommand(new Command("broadcast") {
            @Override
            public void onCommand(CommandSender sender,String[] args) {
                if (sender instanceof Console) {
                    if (args.length > 0) {
                        StringBuilder argString = new StringBuilder();

                        int index = 0;

                        for (String arg : args) {
                            index++;

                            if (index == 1) {
                                argString.append(arg);
                            } else {
                                argString.append(" ");
                                argString.append(arg);
                            }
                        }

                        String message = argString.toString();

                        Server.sendMessage("[Server] :" + message);
                    } else {
                        sender.sendMessage("No message?");
                    }
                }else sender.sendMessage("You don't have permission for this");
            }
        }).setUsage("Sends a broadcast message to all clients");

        server.registerCommand(new Command("ping") {
            @Override
            public void onCommand(CommandSender sender,String[] args) {
                if(sender instanceof Console){
                    ClientPlayer.pingAll();
                }

                if(sender instanceof ClientPlayer) {
                    ClientPlayer clientPlayer = (ClientPlayer) sender;
                    clientPlayer.ping();
                }
            }
        }).setUsage("Sends a ping packet to all clients");

        server.registerCommand(new Command("list") {
            @Override
            public void onCommand(CommandSender sender,String[] args) {
                if(sender instanceof Console) {
                    sender.sendMessage("Players: (" + (PlayerHandler.players.size() ) + ")");

                    for (ClientPlayer clientPlayer : new HashMap<>(Server.socketList).values()) {
                        sender.sendMessage(clientPlayer.getDeviceName() + " :" + clientPlayer.getId() + " { " + clientPlayer.getAdress() + "} Ping:" + clientPlayer.getDelayTime() + "ms");
                    }
                }

                if (sender instanceof ClientPlayer) {
                    String message = "Players: (" + (PlayerHandler.players.size() - 1) + ")";

                    for (ClientPlayer clientPlayer : new HashMap<>(Server.socketList).values()) {
                        if (clientPlayer == null) continue;

                        message = "\n" + clientPlayer.getDeviceName() + " :" + clientPlayer.getId() + " Ping:" + clientPlayer.getDelayTime() + "ms";

                       // sender.sendMessage(clientPlayer.getDeviceName() + " :" + clientPlayer.getId() + " Ping:" + clientPlayer.getDelayTime() + "ms");
                    }

                    sender.sendMessage(message);
                }
            }

        }).setUsage("Lists all players with ip, id and name");

        server.registerCommand(new KickCommand("kick"));

        server.registerCommand(new Command("ban") {
            @Override
            public void onCommand(CommandSender sender,String[] args) {
                if(sender instanceof Console) {

                    if (args.length == 0) {
                        sender.sendMessage("No player to kick or type? (ban {type} {player}) \n types: name,ip");
                    } else {
                        String player = args[0];

                        for (ClientPlayer clientPlayer : new HashMap<>(Server.socketList).values()) {

                            if (player.matches("[0-9]+")) {
                                int id = Integer.parseInt(player);
                                if (id == clientPlayer.getId()) {
                                    StringBuilder message = new StringBuilder();

                                    int index = 0;

                                    for (String messageCheck : args) {
                                        index++;
                                        if (index <= 1) {
                                            message.append(messageCheck);
                                        }
                                    }

                                    Server.getInstance().getBanManager().addBan(clientPlayer,new BannedData(clientPlayer.getAdress()));

                                    clientPlayer.sendObject(new MessagePacket("Banned: " + message));
                                    clientPlayer.close();
                                    break;
                                }
                            }
                        }
                    }

                }else sender.sendMessage("You don't have permission for this");
            }
        }).setUsage("Used to ban players using id. ");

        server.registerCommand(new Command("help") {
            @Override
            public void onCommand(CommandSender sender,String[] args) {
                if(args.length == 0) {
                    sender.sendMessage("Following commands: ");
                    for(Command serverCommand : commandList) {
                        sender.sendMessage(serverCommand.getName());
                    }
                }else{
                    String command = args[0];
                    boolean executed = false;

                    for (Command serverCommand : commandList) {
                        if (serverCommand.getName().equalsIgnoreCase(command)) {
                            if(serverCommand.getUsage().equals("")) {
                                sender.sendMessage("No usage found.");
                            }else
                                sender.sendMessage("Usage: \n" + serverCommand.getUsage());

                            executed = true;
                            break;
                        }
                    }
                    if(!executed) sender.sendMessage("No such command found for getting help");
                }
            }
        }).setUsage("Shows list of commands or usage of a command");



    }

    
    
}
