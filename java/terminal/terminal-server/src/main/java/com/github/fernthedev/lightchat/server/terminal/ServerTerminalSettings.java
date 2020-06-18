package com.github.fernthedev.lightchat.server.terminal;

import com.github.fernthedev.lightchat.server.settings.ServerSettings;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServerTerminalSettings {

    @NonNull
    private ServerSettings serverSettings = new ServerSettings();


    protected boolean lightAllowed = true;
    protected boolean allowChangePassword = true;
    protected boolean allowTermPackets = true;
    protected boolean allowPortArgParse = true;
    protected boolean allowDebugArgParse = true;
    protected int port = -1;

}
