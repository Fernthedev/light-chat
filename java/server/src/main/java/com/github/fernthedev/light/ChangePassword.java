package com.github.fernthedev.light;

import com.github.fernthedev.packets.FillPasswordPacket;
import com.github.fernthedev.server.*;
import com.github.fernthedev.server.backend.BannedData;
import com.github.fernthedev.server.backend.LoggerManager;
import com.github.fernthedev.server.event.EventHandler;
import com.github.fernthedev.server.event.Listener;
import com.github.fernthedev.server.event.chat.ChatEvent;
import com.github.fernthedev.universal.StaticHandler;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Can be used to authenticate command senders.
 */
public class ChangePassword extends Command implements Listener {
    private static HashMap<CommandSender,PlayerInfo> checking = new HashMap<>();

    private static SettingsManager settingsManager;

    public ChangePassword(@NotNull String command,SettingsManager settingsManager) {
        super(command);
        ChangePassword.settingsManager = settingsManager;
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
            sender.sendPacket(new FillPasswordPacket());
        }else if(sender instanceof Console) {
            sender.sendMessage("Type in new password");
            playerInfo.mode = Mode.NEW_PASSWORD;
        }
    }

    public static boolean authenticate(CommandSender sender) {
        if(sender instanceof Console) {
            return true;
        }

        PlayerInfo playerInfo = new PlayerInfo(sender);

        if(!checking.containsKey(sender)) {
            checking.put(sender,playerInfo);
        }

        if(sender instanceof ClientPlayer) {
            playerInfo.mode = Mode.AUTHENTICATE;
            sender.sendMessage("Type in password:");
            sender.sendPacket(new FillPasswordPacket());
        }

        while(checking.containsKey(sender)) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                if(StaticHandler.isDebug) Server.getLogger().error(e.getMessage(),e.getCause());
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
                        settingsManager.getSettings().setPassword(event.getMessage());
                        settingsManager.saveSettings();
                        checking.remove(event.getSender());
                    }else event.getSender().sendMessage("Password can only be alphanumeric");
                }

                if(playerInfo.mode == Mode.OLD_PASSWORD) {
                    if(settingsManager.getSettings().getPassword().equals(event.getMessage())) {
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
                    if(settingsManager.getSettings().getPassword().equals(event.getMessage())) {
                        event.getSender().sendMessage("Correct password. Successfully authenticated:");
                        playerInfo.authenticated = true;
                        checking.remove(event.getSender());
                    }else{
                        if(playerInfo.tries <= 2) {
                            event.getSender().sendMessage("Incorrect password");
                            playerInfo.tries++;
                        }else{
                            LoggerManager.getInstance().log(event.getSender().getName() + ":" + clientPlayer.getAdress() + " tried to authenticate but failed 2 times");
                            checking.remove(event.getSender());
                        }
                    }
                }


            }

            if(event.getSender() instanceof Console) {
                Server.getLogger().info("Setting password now");
                settingsManager.getSettings().setPassword(event.getMessage());
                settingsManager.saveSettings();
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
