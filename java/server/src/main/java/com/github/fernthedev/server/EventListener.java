package com.github.fernthedev.server;

import com.github.fernthedev.packets.*;
import com.github.fernthedev.packets.latency.PongPacket;
import com.github.fernthedev.server.event.chat.ChatEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.fernthedev.server.CommandHandler.commandList;

public class EventListener {

    private Server server;

    private ClientPlayer clientPlayer;
    
    public EventListener(Server server, ClientPlayer clientPlayer) {
        this.server = server;
        this.clientPlayer = clientPlayer;
    }
    
    public void recieved(Object p) {

       // Server.getLogger().info(clientPlayer + " is the sender of packet");

        if(p instanceof TestConnectPacket) {
            TestConnectPacket packet = (TestConnectPacket) p;
            Server.getLogger().info("Connected packet: " + packet.getMessage());
        }

        else if(p instanceof ConnectedPacket) {
            ConnectedPacket packet = (ConnectedPacket)p;
            //Server.getLogger().info("Connected packet recieved from " + clientPlayer.getAdress());
            int id = 1;

            if(PlayerHandler.players.size() > 0) {
                while(id < PlayerHandler.players.size()) {
                    id++;
                }
            }

            if(!isAlphaNumeric(packet.getName())) {
                disconnectIllegalName(packet,"Name requires alphanumeric characters only");
                return;
            }

            for(ClientPlayer player : PlayerHandler.players.values()) {
                if(player.getDeviceName().equalsIgnoreCase(packet.getName())) {
                    disconnectIllegalName(packet,"Name already in use");
                    return;
                }
            }

            if(Server.getInstance().getBanManager().isBanned(clientPlayer)) {
                clientPlayer.sendObject(new MessagePacket("Your have been banned."));
                clientPlayer.close();
                return;
            }

            //Server.getLogger().info("Players: " + PlayerHandler.players.size());

            PlayerHandler.players.put(id, clientPlayer);

            clientPlayer.registered = true;
            clientPlayer.setDeviceName(packet.getName());
            clientPlayer.setId(id);
            clientPlayer.os = packet.getOS();

            Server.getLogger().info(clientPlayer.getDeviceName() + " has connected to the server [" + clientPlayer.os+"]");
            clientPlayer.sendPacket(new RegisterPacket());
            Server.getLogger().debug("NAME:ID " + clientPlayer.getDeviceName() + ":" + clientPlayer.getId());
            Server.getLogger().debug(PlayerHandler.players.get(clientPlayer.getId()).getDeviceName() + " the name." + PlayerHandler.players.get(clientPlayer.getId()).getId() + " the id");
        } else if(p instanceof PongPacket) {

            clientPlayer.endTime = System.nanoTime();

            clientPlayer.delayTime = TimeUnit.NANOSECONDS.toMillis(clientPlayer.endTime - clientPlayer.startTime);



        } else if (p instanceof MessagePacket) {
            MessagePacket messagePacket = (MessagePacket) p;

            ChatEvent chatEvent = new ChatEvent(clientPlayer,messagePacket.getMessage(),false);
            Server.getInstance().getPluginManager().callEvent(chatEvent);

            if(!chatEvent.isCancelled()) {
                Server.sendMessage("[" + clientPlayer.getDeviceName() + "] :" + messagePacket.getMessage());
            }
        } else if (p instanceof CommandPacket) {

            CommandPacket packet = (CommandPacket) p;

            String command = packet.getMessage();

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


            command = command.replaceAll(" {2}"," ");

            if(!command.equals("")) {
                try {
                    ChatEvent chatEvent = new ChatEvent(clientPlayer,command,true,true);
                    Server.getInstance().getPluginManager().callEvent(chatEvent);
                    if(chatEvent.isCancelled()) {
                        return;
                    }

                    for (Command serverCommand : commandList) {
                        if (serverCommand.getCommandName().equalsIgnoreCase(command)) {
                            String[] args = new String[messageword.size()];
                            args = messageword.toArray(args);

                            // Server.getLogger().info("Executing " + command);


                            if(!chatEvent.isCancelled()) {
                                new Thread(new CommandHandler(clientPlayer, serverCommand, args)).start();
                            }
                            break;
                        }
                    }
                } catch (Exception e) {
                    Server.getLogger().error(e.getMessage(),e.getCause());
                }
            }
        }
    }

    public boolean isAlphaNumeric(String name) {
        return StringUtils.isAlphanumericSpace(name.replace('-',' '));
    }

    private void disconnectIllegalName(ConnectedPacket packet,String message) {
        Server.getLogger().info(clientPlayer + " was disconnected for illegal name. Name: " + packet.getName() + " Reason: " + message);
        clientPlayer.sendObject(new IllegalConnection("You have been disconnected for illegal name. Name: " + packet.getName() + " Reason: " + message));
        clientPlayer.close();
    }
}
