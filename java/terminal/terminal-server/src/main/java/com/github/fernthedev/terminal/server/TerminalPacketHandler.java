package com.github.fernthedev.terminal.server;

import com.github.fernthedev.core.data.LightCandidate;
import com.github.fernthedev.core.packets.HashedPasswordPacket;
import com.github.fernthedev.core.packets.Packet;
import com.github.fernthedev.core.packets.handshake.ConnectedPacket;
import com.github.fernthedev.server.ClientPlayer;
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
    public void handlePacket(Packet p, ClientPlayer clientPlayer) {

        if (p instanceof ConnectedPacket) {
            if(server.getSettingsManager().getConfigData().isPasswordRequiredForLogin()) {
                boolean authenticated = ServerTerminal.getAuthenticationManager().authenticate(clientPlayer);
                if(!authenticated) {
                    clientPlayer.sendObject(new MessagePacket("Unable to authenticate"));
                    clientPlayer.close();
                    return;
                }
            }
        } if (p instanceof MessagePacket) {
            MessagePacket messagePacket = (MessagePacket) p;


            ChatEvent chatEvent = new ChatEvent(clientPlayer, messagePacket.getMessage(),false,true);

            server.getPluginManager().callEvent(chatEvent);

            ServerTerminal.getCommandMessageParser().onCommand(chatEvent);
        } else if (p instanceof CommandPacket) {

            CommandPacket packet = (CommandPacket) p;

            String command = packet.getMessage();

            ChatEvent chatEvent = new ChatEvent(clientPlayer,command,true,true);
            server.getPluginManager().callEvent(chatEvent);

            ServerTerminal.getCommandMessageParser().onCommand(chatEvent);
        }else if (p instanceof AutoCompletePacket) {
            AutoCompletePacket packet = (AutoCompletePacket) p;
            List<LightCandidate> candidates = ServerTerminal.getAutoCompleteHandler().handleLine(packet.getWords());

            packet.setCandidateList(candidates);
            clientPlayer.sendObject(packet);
        } else if (p instanceof HashedPasswordPacket) {
            ServerTerminal.getAuthenticationManager().attemptAuthenticationHash(
                    ((HashedPasswordPacket) p).getHashedPassword(),
                    clientPlayer
            );
        }
    }
}
