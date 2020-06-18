package com.github.fernthedev.lightchat.server.terminal;

import com.github.fernthedev.lightchat.server.settings.ServerSettings;
import lombok.*;

@Getter
@Builder
//@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServerTerminalSettings {

    @Builder.Default
    @NonNull
    private ServerSettings serverSettings = new ServerSettings();

    @Builder.Default
    protected boolean lightAllowed = true;

    @Builder.Default
    protected boolean allowChangePassword = true;

    @Builder.Default
    protected boolean allowTermPackets = true;

    @Builder.Default
    protected boolean allowPortArgParse = true;

    @Builder.Default
    protected boolean allowDebugArgParse = true;

    @Builder.Default
    protected int port = -1;

}
