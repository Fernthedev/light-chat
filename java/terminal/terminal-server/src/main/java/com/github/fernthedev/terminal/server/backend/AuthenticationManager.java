package com.github.fernthedev.terminal.server.backend;

import com.github.fernthedev.core.ColorCode;
import com.github.fernthedev.core.data.HashedPassword;
import com.github.fernthedev.core.encryption.util.EncryptionUtil;
import com.github.fernthedev.core.packets.SelfMessagePacket;
import com.github.fernthedev.server.ClientPlayer;
import com.github.fernthedev.server.Console;
import com.github.fernthedev.server.SenderInterface;
import com.github.fernthedev.server.Server;
import com.github.fernthedev.server.event.api.EventHandler;
import com.github.fernthedev.server.event.api.Listener;
import com.github.fernthedev.terminal.server.ServerTerminal;
import com.github.fernthedev.terminal.server.events.ChatEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Can be used to authenticate command senders.
 */
public class AuthenticationManager implements Listener {
    private final Server server;
    private Map<SenderInterface, PlayerInfo> checking = new HashMap<>();

    public AuthenticationManager(Server server) {
        this.server = server;
    }



    public boolean authenticate(SenderInterface sender) {
        if (sender instanceof Console) {
            return true;
        }

        PlayerInfo playerInfo = new PlayerInfo(sender);

        if (!checking.containsKey(sender)) {
            checking.put(sender, playerInfo);
        }



        if (sender instanceof ClientPlayer) {
            playerInfo.mode = Mode.AUTHENTICATE;
            ClientPlayer clientPlayer = (ClientPlayer) sender;
            clientPlayer.sendObject(new SelfMessagePacket(SelfMessagePacket.MessageType.FILL_PASSWORD),false);
            ServerTerminal.sendMessage(sender, "Type in password:");
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

                if(playerInfo.mode == Mode.AUTHENTICATE) {
                    if(server.getSettingsManager().getConfigData().getPassword().equals(event.getMessage())) {
                        ServerTerminal.sendMessage(event.getSender(), ColorCode.GREEN + "Correct password. Successfully authenticated:");
                        playerInfo.authenticated = true;
                        checking.remove(event.getSender());
                    }else{
                        if(playerInfo.tries <= 2) {
                            ServerTerminal.sendMessage(event.getSender(), ColorCode.RED + "Incorrect password");
                            playerInfo.tries++;
                        }else{
                            Server.getLogger().warn(event.getSender().getName() + ":" + clientPlayer.getAddress() + " tried to authenticate but failed 2 times");
//                            LoggerManager.getInstance().log();
                            checking.remove(event.getSender());
                        }
                    }
                }


            }

            if(event.getSender() instanceof Console) {
                server.getLogger().info(ColorCode.GREEN + "Setting password now");
                server.getSettingsManager().getConfigData().setPassword(event.getMessage());
                server.getSettingsManager().save();
                checking.remove(event.getSender());
            }
        }
    }


    /**
     * This requires the password to be hashed, whether the packet is encrypted
     * or not. SelfMessagePacket with FillMessagePacket is sent, it should
     * receive a MessagePacket in return or HashedPasswordPacket with the password hashed.
     * If password given is incorrect, SelfMessagePacket.MessageType.INCORRECT_PASSWORD_ATTEMPT
     * is sent. If authentication finishes with incorrect password,
     * SelfMessagePacket.MessageType.INCORRECT_PASSWORD_FAILURE is sent.
     * @param hashedPassword
     * @param sender
     */
    public void attemptAuthenticationHash(HashedPassword hashedPassword, SenderInterface sender) {
        if(checking.containsKey(sender)) {
            PlayerInfo playerInfo = checking.get(sender);

            if(sender instanceof ClientPlayer) {
                ClientPlayer clientPlayer = (ClientPlayer) sender;
                if(playerInfo.mode == Mode.AUTHENTICATE) {

                    String rightPass = EncryptionUtil.makeSHA256Hash(server.getSettingsManager().getConfigData().getPassword());
                    if(rightPass.equals(hashedPassword.getPassword())) {
                        ServerTerminal.sendMessage(sender, ColorCode.GREEN + "Correct password. Successfully authenticated:");
                        playerInfo.authenticated = true;
                        checking.remove(sender);
                    } else {
                        if(playerInfo.tries <= 2) {
                            ServerTerminal.sendMessage(sender, ColorCode.RED + "Incorrect password");
                            sender.sendPacket(new SelfMessagePacket(SelfMessagePacket.MessageType.INCORRECT_PASSWORD_ATTEMPT));
                            playerInfo.tries++;
                        } else {
                            Server.getLogger().warn("{}:{} tried to authenticate but failed 2 times", sender.getName(), clientPlayer.getAddress());
                            sender.sendPacket(new SelfMessagePacket(SelfMessagePacket.MessageType.INCORRECT_PASSWORD_FAILURE));
                            checking.remove(sender);
                        }
                    }

                }
            }

            if(sender instanceof Console) {
                checking.remove(sender);
            }
        }
    }


    public enum Mode {
        AUTHENTICATE
    }

}
