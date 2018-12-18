package com.github.fernthedev.server;


import com.github.fernthedev.packets.MessagePacket;
import com.github.fernthedev.server.backend.BannedData;
import com.github.fernthedev.server.event.chat.ChatEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import static com.github.fernthedev.server.CommandHandler.commandList;

public class ServerBackground implements Runnable {

    private Server server;
    private Scanner scanner;

    private boolean checked;

    ServerBackground(Server server) {
        this.server = server;
        this.scanner = Main.scanner;
        checked = false;
        Server.getLogger().info("Wait for command thread created");
    }


    public void run() {
        registerCommands();

        while (server.isRunning()) {
            boolean scannerChecked = false;
            //if (scanner.hasNextLine()) {
            if (!checked) {
                Server.getLogger().info("Type Command: (try help)");
                checked = true;
            }
            String command = scanner.nextLine();

            String[] checkmessage = command.split(" ", 2);
            List<String> messageword = new ArrayList<>();


            if (checkmessage.length > 1) {
                String [] messagewordCheck = command.split(" ");

                int index = 0;



                for(String message : messagewordCheck) {
                    if(message == null) continue;

                    message = message.replaceAll(" {2}"," ");

                    index++;
                    if(index == 1 || message.equals("")) continue;


                    messageword.add(message);
                }
            }

            command = checkmessage[0];

            boolean found = false;

            command = command.replaceAll(" {2}"," ");

            if(!command.equals("")) {
                try {
                    ChatEvent chatEvent = new ChatEvent(Server.getInstance().getConsole(),command,true,true);
                    Server.getInstance().getPluginManager().callEvent(chatEvent);
                    if(chatEvent.isCancelled()) found = true;

                    for (Command serverCommand : commandList) {
                        if (serverCommand.getCommandName().equalsIgnoreCase(command)) {
                            found = true;
                            String[] args = new String[messageword.size()];
                            args = messageword.toArray(args);

                           // Server.getLogger().info("Executing " + command);


                            if(!chatEvent.isCancelled()) {
                                new Thread(new CommandHandler(server.getConsole(), serverCommand, args)).start();
                            }
                            break;
                        }
                    }
                } catch (Exception e) {
                    Server.getLogger().error(e.getMessage(),e.getCause());
                }

                if (!found) {
                    Server.getLogger().info("No such command found");
                }
            }
        }
    }


    private void registerCommands() {
        server.registerCommand(new Command("exit") {
            @Override
            public void onCommand(CommandSender sender,String[] args) {
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

                        Server.sendMessage(message);
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
                    sender.sendMessage("Players: (" + (PlayerHandler.players.size() - 1) + ")");

                    for (ClientPlayer clientPlayer : new HashMap<>(Server.socketList).values()) {
                        sender.sendMessage(clientPlayer.getDeviceName() + " :" + clientPlayer.getId() + " { " + clientPlayer.getAdress() + "} Ping:" + clientPlayer.delayTime + "ms");
                    }
                }

                if (sender instanceof ClientPlayer) {
                    sender.sendMessage("Players: (" + (PlayerHandler.players.size() - 1) + ")");

                    for (ClientPlayer clientPlayer : new HashMap<>(Server.socketList).values()) {
                        if (clientPlayer == null) continue;

                        sender.sendMessage(clientPlayer.getDeviceName() + " :" + clientPlayer.getId() + " Ping:" + clientPlayer.delayTime + "ms");
                    }
                }
            }

        }).setUsage("Lists all players with ip, id and name");

        server.registerCommand(new Command("kick") {
            @Override
            public void onCommand(CommandSender sender,String[] args) {
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
                                    sender.sendMessage("Not able to parse number.");
                                }
                            }
                    }
                }else sender.sendMessage("You don't have permission for this");
            }
        }).setUsage("Used to kick players using id");

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
                        sender.sendMessage(serverCommand.getCommandName());
                    }
                }else{
                    String command = args[0];
                    boolean executed = false;

                    for (Command serverCommand : commandList) {
                        if (serverCommand.getCommandName().equalsIgnoreCase(command)) {
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
