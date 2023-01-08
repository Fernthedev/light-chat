package com.github.fernthedev.lightchat.server.terminal;

import com.github.fernthedev.lightchat.core.StaticHandler;
import com.github.fernthedev.lightchat.core.data.LightCandidate;
import com.github.fernthedev.lightchat.core.encryption.PacketTransporter;
import com.github.fernthedev.lightchat.core.packets.HashedPasswordPacket;
import com.github.fernthedev.lightchat.core.packets.Packet;
import com.github.fernthedev.lightchat.core.packets.handshake.ConnectedPacket;
import com.github.fernthedev.lightchat.server.ClientConnection;
import com.github.fernthedev.lightchat.server.Server;
import com.github.fernthedev.lightchat.server.api.IPacketHandler;
import com.github.fernthedev.lightchat.server.terminal.events.ChatEvent;
import com.github.fernthedev.terminal.core.packets.AutoCompletePacket;
import com.github.fernthedev.terminal.core.packets.CommandPacket;
import com.github.fernthedev.terminal.core.packets.MessagePacket;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class TerminalPacketHandler implements IPacketHandler {
    private Server server;

    @Override
    public void handlePacket(Packet p, ClientConnection clientConnection, int packetId) {

        if (p instanceof ConnectedPacket) {
            if(server.getSettingsManager().getConfigData().getPasswordRequiredForLogin()) {
                server.getAuthenticationManager().authenticate(clientConnection).thenAccept(authenticated -> {
                    if(!authenticated) {
                        clientConnection.sendObject(new PacketTransporter(new MessagePacket("Unable to authenticate"), true));
                        clientConnection.close();
                    }
                });

            }
        } if (p instanceof MessagePacket) {
            MessagePacket messagePacket = (MessagePacket) p;

            StaticHandler.getCore().getLogger().debug("Handling message {}", messagePacket.getMessage());
            ChatEvent chatEvent = new ChatEvent(clientConnection, messagePacket.getMessage(),false,true);

            server.getPluginManager().callEvent(chatEvent);

            ServerTerminal.getCommandMessageParser().onCommand(chatEvent);
        } else if (p instanceof CommandPacket) {

            CommandPacket packet = (CommandPacket) p;

            String command = packet.getMessage();

            ChatEvent chatEvent = new ChatEvent(clientConnection,command,true,true);
            server.getPluginManager().callEvent(chatEvent);

            ServerTerminal.getCommandMessageParser().onCommand(chatEvent);
        }else if (p instanceof AutoCompletePacket) {
            AutoCompletePacket packet = (AutoCompletePacket) p;
            List<LightCandidate> candidates = ServerTerminal.getAutoCompleteHandler().handleLine(clientConnection, packet.getWords());

            packet.setCandidateList(candidates);
            clientConnection.sendObject(new PacketTransporter(packet, true));
        } else if (p instanceof HashedPasswordPacket) {
            server.getAuthenticationManager().attemptAuthenticationHash(
                    ((HashedPasswordPacket) p).getHashedPassword(),
                    clientConnection
            );
        }
    }
}
