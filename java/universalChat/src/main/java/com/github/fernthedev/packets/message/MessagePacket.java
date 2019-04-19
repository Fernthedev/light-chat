package com.github.fernthedev.packets.message;

import com.github.fernthedev.packets.Packet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Getter
@Data
public class MessagePacket extends Packet {

    private String message;

    private boolean command;

    public MessagePacket(String message) {
        this(message,false);
    }

}
