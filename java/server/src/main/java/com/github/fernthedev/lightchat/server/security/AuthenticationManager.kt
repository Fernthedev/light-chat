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
import org.jetbrains.annotations.Contract;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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


    /**
     * Runs asynchronously to check if the
     * sender is authenticated
     *
     * Child classes may override this to their liking
     *
     * @param sender authenticator
     * @return future
     *
     * @throws UserIsAuthenticatingException thrown if the user is already attempting authentication
     */
    public CompletableFuture<Boolean> authenticate(SenderInterface sender) {
        if (checking.containsKey(sender)) throw new UserIsAuthenticatingException("The sender " + sender + " is already attempting authentication.");

        if (sender instanceof Console) {
            return CompletableFuture.completedFuture(true);
        }

        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        PlayerInfo playerInfo = new PlayerInfo(sender, completableFuture);



        if (sender instanceof ClientConnection) {
            server.getExecutorService().submit(() -> {

                playerInfo.mode = Mode.AUTHENTICATE;
                AuthenticateRequestEvent event = new AuthenticateRequestEvent(playerInfo, true);

                server.getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    completableFuture.complete(playerInfo.authenticated);
                    return;
                }

                if (!checking.containsKey(sender)) {
                    checking.put(sender, playerInfo);
                }

                ClientConnection clientConnection = (ClientConnection) sender;
                clientConnection.sendObject(new SelfMessagePacket(SelfMessagePacket.MessageType.FILL_PASSWORD), false);


            });
        }

        return completableFuture;
    }


    /**
     * Gets the future of the authentication
     * process for the sender if they are being authenticated
     *
     * @return null if not being authenticated
     */
    @Contract("null -> null")
    public CompletableFuture<Boolean> getUserAuthenticationFuture(SenderInterface sender) {
        return checking.get(sender).future;
    }

    /**
     * This requires the password to be hashed, whether the packet is encrypted
     * or not. SelfMessagePacket with FillMessagePacket is sent, it should
     * receive a MessagePacket in return or HashedPasswordPacket with the password hashed.
     * If password given is incorrect, SelfMessagePacket.MessageType.INCORRECT_PASSWORD_ATTEMPT
     * is sent. If authentication finishes with incorrect password,
     * SelfMessagePacket.MessageType.INCORRECT_PASSWORD_FAILURE is sent.
     *
     * @param hashedPassword
     * @param sender
     */
    public void attemptAuthenticationHash(HashedPassword hashedPassword, SenderInterface sender) {
        if (checking.containsKey(sender)) {
            PlayerInfo playerInfo = checking.get(sender);

            AuthenticationAttemptedEvent event = new AuthenticationAttemptedEvent(playerInfo, true, null);


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
                        completeAuthentication(playerInfo, true);

                        sender.sendPacket(new SelfMessagePacket(SelfMessagePacket.MessageType.CORRECT_PASSWORD));
                        break;
                    case ATTEMPT_FAILED:
                        sender.sendPacket(new SelfMessagePacket(SelfMessagePacket.MessageType.INCORRECT_PASSWORD_ATTEMPT));
                        playerInfo.tries++;

                        break;
                    case NO_MORE_TRIES:

                        String info;

                        if (sender instanceof ClientConnection) info = ((ClientConnection) sender).getAddress();
                        else info = "";

                        server.getLogger().warn("{}:{} tried to authenticate but failed 2 times", sender.getName(), info);
                        sender.sendPacket(new SelfMessagePacket(SelfMessagePacket.MessageType.INCORRECT_PASSWORD_FAILURE));
                        completeAuthentication(playerInfo, false);
                        break;
                }
            }
        }
    }

    protected void completeAuthentication(PlayerInfo playerInfo, boolean authenticated) {
        playerInfo.future.complete(authenticated);
        checking.remove(playerInfo.sender);
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

        protected CompletableFuture<Boolean> future;

        public final SenderInterface sender;

        public boolean authenticated = false;

        public AuthenticationManager.Mode mode = AuthenticationManager.Mode.AUTHENTICATE;
        public int tries = 0;



        public PlayerInfo(SenderInterface sender, CompletableFuture<Boolean> completableFuture) {
            this.sender = sender;
            this.future = completableFuture;
        }



    }

}
