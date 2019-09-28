package com.github.fernthedev.light;

import com.github.fernthedev.packets.SelfMessagePacket;
import com.github.fernthedev.server.ClientPlayer;
import com.github.fernthedev.server.Console;
import com.github.fernthedev.server.Server;
import com.github.fernthedev.server.backend.BannedData;
import com.github.fernthedev.server.backend.LoggerManager;
import com.github.fernthedev.server.command.Command;
import com.github.fernthedev.server.command.CommandSender;
import com.github.fernthedev.server.event.EventHandler;
import com.github.fernthedev.server.event.Listener;
import com.github.fernthedev.server.event.chat.ChatEvent;
import com.github.fernthedev.universal.ColorCode;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Can be used to authenticate command senders.
 */
public class AuthenticationManager extends Command implements Listener {
    private static Map<CommandSender,PlayerInfo> checking = new HashMap<>();

    public AuthenticationManager(@NonNull String command) {
        super(command);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        PlayerInfo playerInfo = new PlayerInfo(sender);

        if(!checking.containsKey(sender)) {
            checking.put(sender,playerInfo);
        }


        if(sender instanceof ClientPlayer) {
            playerInfo.mode = Mode.OLD_PASSWORD;
            sender.sendMessage("Type in old password:");
            sender.sendPacket(new SelfMessagePacket(SelfMessagePacket.MessageType.FILL_PASSWORD));
        }else if(sender instanceof Console) {
            sender.sendMessage("Type in new password");
            playerInfo.mode = Mode.NEW_PASSWORD;
        }
    }

    public static boolean authenticate(CommandSender sender) {

        if (sender instanceof Console) {
            return true;
        }

        PlayerInfo playerInfo = new PlayerInfo(sender);

        if (!checking.containsKey(sender)) {
            checking.put(sender, playerInfo);
        }



        if (sender instanceof ClientPlayer) {
            playerInfo.mode = Mode.AUTHENTICATE;
//            Server.getLogger().info("Sending fill password");
            ClientPlayer clientPlayer = (ClientPlayer) sender;
            clientPlayer.sendObject(new SelfMessagePacket(SelfMessagePacket.MessageType.FILL_PASSWORD),false);
            sender.sendMessage("Type in password:");
        }

        while (checking.containsKey(sender)) {
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return playerInfo.authenticated;
    }

    @EventHandler
    public void onChatEvent(ChatEvent event) {
        if(checking.containsKey(event.getSender())) {

            event.setCancelled(true);
            PlayerInfo playerInfo = checking.get(event.getSender());

            if(event.getSender() instanceof ClientPlayer) {
                ClientPlayer clientPlayer = (ClientPlayer) event.getSender();

                if(playerInfo.mode == Mode.NEW_PASSWORD) {
                    if(StringUtils.isAlphanumeric(event.getMessage())) {
                        event.getSender().sendMessage("Setting password now");
                        Server.getSettingsManager().getConfigData().setPassword(event.getMessage());
                        Server.getSettingsManager().save();
                        checking.remove(event.getSender());
                    }else event.getSender().sendMessage("Password can only be alphanumeric");
                }

                if(playerInfo.mode == Mode.OLD_PASSWORD) {
                    if(Server.getSettingsManager().getConfigData().getPassword().equals(event.getMessage())) {
                        event.getSender().sendMessage("Correct password. Choose new password:");
                        playerInfo.mode = Mode.NEW_PASSWORD;
                    }else{
                        if(playerInfo.tries <= 2) {
                            event.getSender().sendMessage("Incorrect password");
                            playerInfo.tries++;
                        }else{
                            Server.getInstance().getBanManager().addBan(clientPlayer,new BannedData(clientPlayer.getAdress()));
                            checking.remove(event.getSender());
                        }
                    }
                }

                if(playerInfo.mode == Mode.AUTHENTICATE) {
                    if(Server.getSettingsManager().getConfigData().getPassword().equals(event.getMessage())) {
                        event.getSender().sendMessage(ColorCode.GREEN + "Correct password. Successfully authenticated:");
                        playerInfo.authenticated = true;
                        checking.remove(event.getSender());
                    }else{
                        if(playerInfo.tries <= 2) {
                            event.getSender().sendMessage(ColorCode.RED + "Incorrect password");
                            playerInfo.tries++;
                        }else{
                            LoggerManager.getInstance().log(event.getSender().getName() + ":" + clientPlayer.getAdress() + " tried to authenticate but failed 2 times");
                            checking.remove(event.getSender());
                        }
                    }
                }


            }

            if(event.getSender() instanceof Console) {
                Server.getLogger().info(ColorCode.GREEN + "Setting password now");
                Server.getSettingsManager().getConfigData().setPassword(event.getMessage());
                Server.getSettingsManager().save();
                checking.remove(event.getSender());
            }
        }
    }


    public enum Mode {
        OLD_PASSWORD,
        NEW_PASSWORD,
        AUTHENTICATE
    }

}
