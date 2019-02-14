package com.github.fernthedev.packets;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RequestInfoPacket extends Packet {

    private String key;

}
