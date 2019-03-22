package com.github.fernthedev.packets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessagePacket extends Packet {

    public MessagePacket(String message) {
        this.message.add(message);
    }

    public MessagePacket(String message1,String... messageList) {
        message.add(message1);
        message.addAll(Arrays.asList(messageList));
    }

    private List<String> message = new ArrayList<>();

    public List<String> getMessage() {
        return message;
    }

    public String listToString() {
        StringBuilder string = new StringBuilder();
        for(String s : message) {
            string.append(s);
        }
        return string.toString();
    }

}
