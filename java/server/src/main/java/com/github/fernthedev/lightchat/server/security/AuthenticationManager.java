package com.github.fernthedev.lightchat.server.security;

import com.github.fernthedev.lightchat.core.api.event.api.Listener;
import com.github.fernthedev.lightchat.core.data.HashedPassword;
import com.github.fernthedev.lightchat.core.encryption.util.EncryptionUtil;
import com.github.fernthedev.lightchat.core.packets.SelfMessagePacket;
import com.github.fernthedev.lightchat.server.ClientConnection;
import com.github.fernthedev.lightchat.server.Console;
import com.github.fernthedev.lightchat.server.SenderInterface;
import com.github.fernthedev.lightchat.server.Server;
import com.github.fernthedev.lightchat.server.event.AuthenticateRequestEvent;
import com.github.fernthedev.lightchat.server.event.AuthenticationAttemptedEvent;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Can be used to authenticate command senders.
 */
public class AuthenticationManager implements Listener {
    private final Server server;

    private final Map<SenderInterface, PlayerInfo> checking = new HashMap<>();

    public AuthenticationManager(Server server) {
        this.server = server;
    }

    @Getter
    protected int amountOfTries = 2;


    public boolean authenticate(SenderInterface sender) {
        if (sender instanceof Console) {
            return true;
        }

        PlayerInfo playerInfo = new PlayerInfo(sender);

        if (!checking.containsKey(sender)) {
            checking.put(sender, playerInfo);
        }



        if (sender instanceof ClientConnection) {
            playerInfo.mode = Mode.AUTHENTICATE;
            AuthenticateRequestEvent event = new AuthenticateRequestEvent(playerInfo, true);

            server.getPluginManager().callEvent(event);

            if (event.isCancelled()) return playerInfo.authenticated;

            ClientConnection clientConnection = (ClientConnection) sender;
            clientConnection.sendObject(new SelfMessagePacket(SelfMessagePacket.MessageType.FILL_PASSWORD),false);

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
        if (checking.containsKey(sender)) {
            PlayerInfo playerInfo = checking.get(sender);

            AuthenticationAttemptedEvent event = new AuthenticationAttemptedEvent(playerInfo, true, null);


            if (sender instanceof ClientConnection) {
                ClientConnection clientConnection = (ClientConnection) sender;
                if (playerInfo.mode == Mode.AUTHENTICATE) {

                    String rightPass = EncryptionUtil.makeSHA256Hash(server.getSettingsManager().getConfigData().getPassword());

                    AuthenticationAttemptedEvent.EventStatus eventStatus;

                    if (rightPass.equals(hashedPassword.getPassword()))
                        eventStatus = AuthenticationAttemptedEvent.EventStatus.SUCCESS;
                    else
                        eventStatus = playerInfo.tries <= amountOfTries ?
                                AuthenticationAttemptedEvent.EventStatus.ATTEMPT_FAILED :
                                AuthenticationAttemptedEvent.EventStatus.NO_MORE_TRIES;

                    //Handle event
                    event.setEventStatus(eventStatus);

                    server.getPluginManager().callEvent(event);

                    if (event.isCancelled()) return;


                    playerInfo.authenticated = event.getEventStatus() == AuthenticationAttemptedEvent.EventStatus.SUCCESS;

                    switch (event.getEventStatus()) {

                        case SUCCESS:
                            checking.remove(sender);

                            sender.sendPacket(new SelfMessagePacket(SelfMessagePacket.MessageType.CORRECT_PASSWORD));
                            break;
                        case ATTEMPT_FAILED:
                            sender.sendPacket(new SelfMessagePacket(SelfMessagePacket.MessageType.INCORRECT_PASSWORD_ATTEMPT));
                            playerInfo.tries++;
                            break;
                        case NO_MORE_TRIES:
                            server.getLogger().warn("{}:{} tried to authenticate but failed 2 times", sender.getName(), clientConnection.getAddress());
                            sender.sendPacket(new SelfMessagePacket(SelfMessagePacket.MessageType.INCORRECT_PASSWORD_FAILURE));
                            checking.remove(sender);
                            break;
                    }
                }
            }

            if (sender instanceof Console) {
                server.getPluginManager().callEvent(event);

                if (event.isCancelled()) return;

                checking.remove(sender);
            }
        }
    }

    /**
     * Returns a copy of the authentication queue
     * @return copy of authentication queue
     */
    public Map<SenderInterface, PlayerInfo> getAwaitingAuthentications() {
        return new HashMap<>(checking);
    }

    public enum Mode {
        AUTHENTICATE
    }

    public static class PlayerInfo {

        public final SenderInterface sender;

        public boolean authenticated = false;

        public AuthenticationManager.Mode mode = AuthenticationManager.Mode.AUTHENTICATE;
        public int tries = 0;



        public PlayerInfo(SenderInterface sender) {
            this.sender = sender;
        }



    }

}
