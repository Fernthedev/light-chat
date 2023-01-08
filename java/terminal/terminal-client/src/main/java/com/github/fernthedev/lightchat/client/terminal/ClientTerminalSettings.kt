package com.github.fernthedev.lightchat.client.terminal;

import com.github.fernthedev.config.common.Config;
import com.github.fernthedev.config.gson.GsonConfig;
import com.github.fernthedev.lightchat.client.ClientSettings;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.File;

@Getter
@Builder
public class ClientTerminalSettings {

    @Builder.Default
    protected String host = null;

    @Builder.Default
    protected int port = -1;

    @Builder.Default
    @NonNull
    protected Config<? extends ClientSettings> clientSettings = createConfigWithoutException();


    @Builder.Default
    protected boolean allowTermPackets = true;

    @Builder.Default
    protected boolean launchConsoleInCMDWhenNone = true;

    @Builder.Default
    protected boolean consoleCommandHandler = true;

    @Builder.Default
    protected boolean checkForServersInMulticast = true;

    @Builder.Default
    protected boolean askUserForHostPort = true;

    @Builder.Default
    protected boolean shutdownOnDisconnect = true;

    @SneakyThrows
    protected static Config<? extends ClientSettings> createConfigWithoutException() {
        return new GsonConfig<>(new ClientSettings(), new File("client_settings.json"));
    }

}
