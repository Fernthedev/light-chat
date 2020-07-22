package com.github.fernthedev.lightchat.client.terminal;

import com.github.fernthedev.lightchat.client.api.IPacketHandler;
import com.github.fernthedev.lightchat.client.event.ServerDisconnectEvent;
import com.github.fernthedev.lightchat.core.ColorCode;
import com.github.fernthedev.lightchat.core.StaticHandler;
import com.github.fernthedev.lightchat.core.api.event.api.EventHandler;
import com.github.fernthedev.lightchat.core.api.event.api.Listener;
import com.github.fernthedev.lightchat.core.packets.Packet;
import com.github.fernthedev.lightchat.core.packets.SelfMessagePacket;
import com.github.fernthedev.terminal.core.packets.AutoCompletePacket;
import com.github.fernthedev.terminal.core.packets.MessagePacket;
import lombok.AllArgsConstructor;

import java.util.concurrent.TimeUnit;

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

            if (ClientTerminal.getMessageDelay().isRunning())
                ClientTerminal.getMessageDelay().stop();

            ClientTerminal.getLogger().debug("Time taken for message: {}", ClientTerminal.getMessageDelay().elapsed(TimeUnit.MILLISECONDS));

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
