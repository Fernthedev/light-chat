package com.github.fernthedev.lightchat.server.terminal.command;

import com.github.fernthedev.config.common.exceptions.ConfigLoadException;
import com.github.fernthedev.lightchat.core.ColorCode;
import com.github.fernthedev.lightchat.core.api.event.api.EventHandler;
import com.github.fernthedev.lightchat.core.api.event.api.Listener;
import com.github.fernthedev.lightchat.core.data.HashedPassword;
import com.github.fernthedev.lightchat.server.SenderInterface;
import com.github.fernthedev.lightchat.server.Server;
import com.github.fernthedev.lightchat.server.event.AuthenticationAttemptedEvent;
import com.github.fernthedev.lightchat.server.security.AuthenticationManager;
import com.github.fernthedev.lightchat.server.terminal.ServerTerminal;
import com.github.fernthedev.lightchat.server.terminal.events.ChatEvent;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class AuthTerminalHandler extends Command implements Listener {

    private Server server;

    public AuthTerminalHandler(@NonNull String name, Server server) {
        super(name);
        this.server = server;
    }

    @Override
    public void onCommand(SenderInterface sender, String[] args) {

        if (args.length == 0) {
            ServerTerminal.sendMessage(sender, "Please provide new password");
            return;
        }

        if (StringUtils.isAlphanumeric(args[0])) {
            server.getAuthenticationManager().authenticate(sender).thenAccept(aBoolean -> {
                if (aBoolean) {
                    ServerTerminal.sendMessage(sender, "Setting password now");
                    server.getSettingsManager().getConfigData().setPassword(args[0]);
                    try {
                        server.getSettingsManager().save();
                    } catch (ConfigLoadException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else ServerTerminal.sendMessage(sender, "Password can only be alphanumeric");
    }

    @EventHandler
    public void onChatEvent(ChatEvent event) {
        AuthenticationManager authenticationManager = server.getAuthenticationManager();

        Map<SenderInterface, AuthenticationManager.PlayerInfo> checking = authenticationManager.getAwaitingAuthentications();

        if(checking.containsKey(event.getSender())) {

            event.setCancelled(true);

            server.getAuthenticationManager().attemptAuthenticationHash(new HashedPassword(event.getMessage()), event.getSender());
        }
    }

    @EventHandler
    public void onAuthenticateEvent(AuthenticationAttemptedEvent e) {
        switch (e.getEventStatus()) {

            case SUCCESS:
                // Success
                ServerTerminal.sendMessage(e.getPlayerInfo().sender, ColorCode.GREEN + "Correct password. Successfully authenticated:");
                break;
            case ATTEMPT_FAILED:
                ServerTerminal.sendMessage(e.getPlayerInfo().sender, ColorCode.RED + "Incorrect password");
                break;
            case NO_MORE_TRIES:
                ServerTerminal.sendMessage(e.getPlayerInfo().sender, ColorCode.RED + "Too many tries. Failed to authenticate");
                break;
        }

    }
}
