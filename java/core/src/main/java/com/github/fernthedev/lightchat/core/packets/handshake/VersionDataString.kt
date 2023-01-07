package com.github.fernthedev.lightchat.core.packets.handshake;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
class VersionDataString implements Serializable {
    private String version;
    private String minVersion;
}
