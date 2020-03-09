package com.github.fernthedev.terminal.server;

import com.github.fernthedev.core.StaticHandler;
import com.github.fernthedev.core.data.LightCandidate;
import com.github.fernthedev.core.packets.HashedPasswordPacket;
import com.github.fernthedev.core.packets.Packet;
import com.github.fernthedev.core.packets.handshake.ConnectedPacket;
import com.github.fernthedev.server.ClientConnection;
import com.github.fernthedev.server.Server;
import com.github.fernthedev.server.api.IPacketHandler;
import com.github.fernthedev.terminal.core.packets.AutoCompletePacket;
import com.github.fernthedev.terminal.core.packets.CommandPacket;
import com.github.fernthedev.terminal.core.packets.MessagePacket;
import com.github.fernthedev.terminal.server.events.ChatEvent;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class TerminalPacketHandler implements IPacketHandler {
    private Server server;

    @Override
    public void handlePacket(Packet p, ClientConnection clientConnection, int packetId) {

        if (p instanceof ConnectedPacket) {
            if(server.getSettingsManager().getConfigData().isPasswordRequiredForLogin()) {
                boolean authenticated = ServerTerminal.getAuthenticationManager().authenticate(clientConnection);
                if(!authenticated) {
                    clientConnection.sendObject(new MessagePacket("Unable to authenticate"));
                    clientConnection.close();
                    return;
                }
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
            List<LightCandidate> candidates = ServerTerminal.getAutoCompleteHandler().handleLine(packet.getWords());

            packet.setCandidateList(candidates);
            clientConnection.sendObject(packet);
        } else if (p instanceof HashedPasswordPacket) {
            ServerTerminal.getAuthenticationManager().attemptAuthenticationHash(
                    ((HashedPasswordPacket) p).getHashedPassword(),
                    clientConnection
            );
        }
    }
}
