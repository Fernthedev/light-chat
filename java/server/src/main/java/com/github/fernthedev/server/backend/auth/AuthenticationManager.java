package com.github.fernthedev.server.backend.auth;

import com.github.fernthedev.packets.FillPasswordPacket;
import com.github.fernthedev.server.ClientPlayer;
import com.github.fernthedev.server.Console;
import com.github.fernthedev.server.Server;
import com.github.fernthedev.server.backend.SettingsManager;
import com.github.fernthedev.server.command.Command;
import com.github.fernthedev.server.command.CommandSender;
import com.github.fernthedev.server.event.EventHandler;
import com.github.fernthedev.server.event.Listener;
import com.github.fernthedev.server.event.chat.ChatEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Can be used to authenticate command senders.
 */
public class AuthenticationManager extends Command implements Listener {
    private static Map<CommandSender, PlayerInfo> checking = new HashMap<>();

    private static Map<CommandSender,Date> authenticatedCache = new HashMap<>();

    private static SettingsManager settingsManager;

    public AuthenticationManager(@NotNull String command, SettingsManager settingsManager) {
        super(command);
        AuthenticationManager.settingsManager = settingsManager;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        boolean authenticated = authenticate(sender);

        if(authenticated) {
            setNewPassword(sender);
        }
    }

    public static boolean authenticate(CommandSender sender) {
        if (sender instanceof Console) {
            return true;
        }

        if(authenticatedCache.containsKey(sender)) {
            return true;
        }

        PlayerInfo playerInfo = new PlayerInfo(sender);

        if (!checking.containsKey(sender)) {
            checking.put(sender, playerInfo);
        }



        if (sender instanceof ClientPlayer) {
            playerInfo.mode = Mode.AUTHENTICATE;
            ClientPlayer clientPlayer = (ClientPlayer) sender;
            clientPlayer.sendObject(new FillPasswordPacket(),false);
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

    private static void setNewPassword(CommandSender sender) {
        PlayerInfo playerInfo = new PlayerInfo(sender);

        if (!checking.containsKey(sender)) {
            checking.put(sender, playerInfo);
        }


        playerInfo.mode = Mode.NEW_PASSWORD;
        sender.sendMessage("Choose new password:");


        while (checking.containsKey(sender)) {
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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

                if(playerInfo.mode == Mode.AUTHENTICATE) {
                    if(settingsManager.getSettings().getPassword().equals(event.getMessage())) {
                        event.getSender().sendMessage("Correct password. Successfully authenticated:");
                        playerInfo.authenticated = true;
                        checking.remove(event.getSender());

                        setupAuthCache(clientPlayer);


                    }else{
                        if(playerInfo.tries <= 2) {
                            event.getSender().sendMessage("Incorrect password");
                            playerInfo.tries++;
                        }else{
                            //LogFileManager.log(event.getSender().getName() + ":" + clientPlayer.getAdress() + " tried to authenticate but failed 2 times");
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


    private static void setupAuthCache(ClientPlayer clientPlayer) {
        Timer timer = new Timer();

        Date date = new Date();

        authenticatedCache.remove(clientPlayer);



        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(DateUtils.addSeconds(date,30));

        authenticatedCache.put(clientPlayer,calendar.getTime());

        Thread thread = new Thread(() -> {
            if(calendar.getTime().getTime() >= authenticatedCache.get(clientPlayer).getTime()) {
                Server.getLogger().info("Going to remove");
                authenticatedCache.remove(clientPlayer);
            }else{
                Server.getLogger().info("Not removing, time update");
            }
        });

// Schedule to run every Sunday in midnight
        timer.schedule(
                new ThreadTask (thread),
                calendar.getTime()
        );

    }

    public enum Mode {
        OLD_PASSWORD,
        NEW_PASSWORD,
        AUTHENTICATE
    }


}
