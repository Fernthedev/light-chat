package com.github.fernthedev.lightchat.server.terminal;

import com.github.fernthedev.config.common.Config;
import com.github.fernthedev.config.gson.GsonConfig;
import com.github.fernthedev.lightchat.server.settings.ServerSettings;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.File;

@Getter
@Builder
//@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServerTerminalSettings {

    @Builder.Default
    @NonNull
    private Config<? extends ServerSettings> serverSettings = createConfigWithoutException();


    @Builder.Default
    protected boolean allowChangePassword = true;

    @Builder.Default
    protected boolean allowTermPackets = true;

    @Builder.Default
    protected boolean launchConsoleInCMDWhenNone = true;

    @Builder.Default
    protected boolean consoleCommandHandler = true;

    @Builder.Default
    protected int port = -1;


    @SneakyThrows
    protected static Config<? extends ServerSettings> createConfigWithoutException() {
        return new GsonConfig<>(new ServerSettings(), new File("settings.json"));
    }
}
