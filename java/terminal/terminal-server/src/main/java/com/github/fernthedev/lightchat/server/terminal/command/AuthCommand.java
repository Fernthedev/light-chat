package com.github.fernthedev.lightchat.server.terminal.command;

import com.github.fernthedev.config.common.exceptions.ConfigLoadException;
import com.github.fernthedev.lightchat.server.SenderInterface;
import com.github.fernthedev.lightchat.server.Server;
import com.github.fernthedev.lightchat.server.terminal.ServerTerminal;
import com.github.fernthedev.lightchat.server.terminal.backend.AuthenticationManager;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

public class AuthCommand extends Command {

    private Server server;
    private AuthenticationManager authenticationManager;

    public AuthCommand(@NonNull String name, Server server) {
        super(name);
        this.server = server;
        this.authenticationManager = ServerTerminal.getAuthenticationManager();
    }

    @Override
    public void onCommand(SenderInterface sender, String[] args) {

        if (args.length == 0) {
            ServerTerminal.sendMessage(sender, "Please provide new password");
            return;
        }

        if (StringUtils.isAlphanumeric(args[0])) {
            if (authenticationManager.authenticate(sender)) {
                ServerTerminal.sendMessage(sender, "Setting password now");
                server.getSettingsManager().getConfigData().setPassword(args[0]);
                try {
                    server.getSettingsManager().save();
                } catch (ConfigLoadException e) {
                    e.printStackTrace();
                }
            }
        } else ServerTerminal.sendMessage(sender, "Password can only be alphanumeric");
    }
}
