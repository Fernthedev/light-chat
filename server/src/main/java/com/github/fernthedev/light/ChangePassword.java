package com.github.fernthedev.light;

import com.github.fernthedev.server.*;
import com.github.fernthedev.server.backend.BannedData;
import com.github.fernthedev.server.event.EventHandler;
import com.github.fernthedev.server.event.Listener;
import com.github.fernthedev.server.event.chat.ChatEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ChangePassword extends Command implements Listener {
    private static HashMap<CommandSender,PlayerInfo> checking = new HashMap<>();

    private static LightManager lightManager;

    public ChangePassword(@NotNull String command,LightManager lightManager) {
        super(command);
        ChangePassword.lightManager = lightManager;
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
        }else if(sender instanceof Console) sender.sendMessage("Type in new password");
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
                        lightManager.getSettings().setPassword(event.getMessage());
                        lightManager.saveSettings();
                        checking.remove(event.getSender());
                    }else event.getSender().sendMessage("Password can only be alphanumeric");
                }

                if(playerInfo.mode == Mode.OLD_PASSWORD) {
                    if(lightManager.getSettings().getPassword().equals(event.getMessage())) {
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


            }

            if(event.getSender() instanceof Console) {
                Server.getLogger().info("Setting password now");
                lightManager.getSettings().setPassword(event.getMessage());
                lightManager.saveSettings();
                checking.remove(event.getSender());
            }
        }
    }

    private enum Mode {
        OLD_PASSWORD,
        NEW_PASSWORD
    }

    private class PlayerInfo {

        private final CommandSender sender;

        private Mode mode = Mode.NEW_PASSWORD;
        private int tries = 0;



        public PlayerInfo(CommandSender sender) {
            this.sender = sender;
        }

    }
}
