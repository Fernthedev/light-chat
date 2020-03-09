package com.github.fernthedev.terminal.client;

import com.github.fernthedev.client.api.IPacketHandler;
import com.github.fernthedev.client.event.ServerDisconnectEvent;
import com.github.fernthedev.core.ColorCode;
import com.github.fernthedev.core.StaticHandler;
import com.github.fernthedev.core.api.event.api.EventHandler;
import com.github.fernthedev.core.api.event.api.Listener;
import com.github.fernthedev.core.packets.Packet;
import com.github.fernthedev.core.packets.SelfMessagePacket;
import com.github.fernthedev.terminal.core.packets.AutoCompletePacket;
import com.github.fernthedev.terminal.core.packets.MessagePacket;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PacketHandler implements IPacketHandler, Listener {

    @Override
    public void handlePacket(Packet p, int packetId) {
        if(p instanceof AutoCompletePacket) {
            AutoCompletePacket packet = (AutoCompletePacket) p;
            ClientTerminal.getAutoCompleteHandler().addCandidates(packet.getCandidateList());
        } else if (p instanceof MessagePacket) {
            MessagePacket messagePacket = (MessagePacket) p;
            ClientTerminal.getLogger().info(messagePacket.getMessage());
        } else if (p instanceof SelfMessagePacket) {
            SelfMessagePacket selfMessagePacket = (SelfMessagePacket) p;

            switch (selfMessagePacket.getType()) {
                case INCORRECT_PASSWORD_FAILURE:
                    ClientTerminal.getLogger().error(ColorCode.RED + "Failed all attempts to login.");
            }
        }
    }

    @EventHandler
    public void onDisconnect(ServerDisconnectEvent e) {
        StaticHandler.getCore().getLogger().info("CLOSING CLIENT");
        System.exit(0);
    }
}
