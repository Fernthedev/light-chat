package com.github.fernthedev.lightchat.server

import com.github.fernthedev.lightchat.core.StaticHandler
import com.github.fernthedev.lightchat.core.VersionData
import com.github.fernthedev.lightchat.core.encryption.transport
import com.github.fernthedev.lightchat.core.exceptions.ParsePacketException
import com.github.fernthedev.lightchat.core.packets.IllegalConnectionPacket
import com.github.fernthedev.lightchat.core.packets.Packet
import com.github.fernthedev.lightchat.core.packets.SelfMessagePacket
import com.github.fernthedev.lightchat.core.packets.handshake.ConnectedPacket
import com.github.fernthedev.lightchat.core.packets.handshake.KeyResponsePacket
import com.github.fernthedev.lightchat.core.packets.handshake.RequestConnectInfoPacket
import com.github.fernthedev.lightchat.core.packets.latency.PingReceive
import com.github.fernthedev.lightchat.core.packets.latency.PongPacket
import com.github.fernthedev.lightchat.core.util.ExceptionUtil
import com.github.fernthedev.lightchat.server.event.PlayerJoinEvent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils

class EventListener(private val server: Server, private val clientConnection: ClientConnection) {
    suspend fun received(p: Packet, packetId: Int) = coroutineScope {
        try {
            //Packet p = (Packet) EncryptionHandler.decrypt(pe, clientConnection.getServerKey());


            // server.logInfo(clientConnection + " is the sender of packet");
            if (p is PongPacket) {
                clientConnection.finishPing()
                clientConnection.sendPacket(PingReceive().transport())
            } else if (p is KeyResponsePacket) {
                StaticHandler.core.logger.debug("Received key")
                clientConnection.secretKey = p.getSecretKey(clientConnection.tempKeyPair!!.private)
                clientConnection.sendPacket(RequestConnectInfoPacket().transport())
            }


            server.packetHandlers.forEach { iPacketHandler ->
                try {
                    iPacketHandler.handlePacket(p, clientConnection, packetId)
                } catch (e: Exception) {
                    throw ExceptionUtil.throwParsePacketException(e, p)
                }
            }
        } catch (e: ParsePacketException) {
            throw e
        } catch (e: Exception) {
            throw ExceptionUtil.throwParsePacketException(e, p)
        }
    }

    suspend fun handleConnect(packet: ConnectedPacket): Unit = coroutineScope {
        try {
            if (!isAlphaNumeric(packet.name)) {
                disconnectIllegalName(
                    packet,
                    "Name requires alphanumeric characters only. (dashes, periods and @ symbols are allowed)"
                )
                return@coroutineScope
            }
            for (player in server.playerHandler.uuidMap.values) {
                if (player.name.equals(packet.name, ignoreCase = true)) {
                    disconnectIllegalName(packet, "Name already in use")
                    return@coroutineScope
                }
            }
            clientConnection.finishConstruct(packet.name, packet.os, packet.langFramework)
            val versionData = packet.versionData
            val versionRange = StaticHandler.getVersionRangeStatus(VersionData(versionData))
            if (versionRange == StaticHandler.VersionRange.MATCH_REQUIREMENTS) server.logInfo(
                "{}'s version range requirements match Server version.",
                clientConnection
            ) else {
                // The current version is larger than client's minimum version
                if (versionRange == StaticHandler.VersionRange.WE_ARE_HIGHER) {
                    server.logInfo(
                        "{}'s version ({}) does not meet server's minimum version ({}) requirements. Expect incompatibility issues",
                        clientConnection,
                        versionData.version,
                        StaticHandler.VERSION_DATA.minVersion
                    )
                }


                // The current version is smaller than the client's required minimum
                if (versionRange == StaticHandler.VersionRange.WE_ARE_LOWER) {
                    server.logInfo(
                        "The server version ({}) does not meet {}'s minimum version ({}) requirements. Expect incompatibility issues",
                        StaticHandler.VERSION_DATA.version,
                        clientConnection,
                        versionData.minVersion
                    )
                }
            }
            server.playerHandler.uuidMap[clientConnection.uuid] = clientConnection
            clientConnection.registered = true
            server.logInfo(
                "{} has connected to the server [{} | {}] ",
                clientConnection.name,
                clientConnection.os,
                clientConnection.langFramework
            )
            clientConnection.sendPacket(SelfMessagePacket(SelfMessagePacket.MessageType.REGISTER_PACKET).transport())

            launch {
                server.pluginManager.callEvent(
                    PlayerJoinEvent(
                        clientConnection, true
                    )
                )
            }
        } catch (e: Exception) {
            throw ExceptionUtil.throwParsePacketException(e, packet)
        }
    }

    private fun isAlphaNumeric(name: String): Boolean {
        return StringUtils.isAlphanumericSpace(
            name.replace('-', ' ')
                .replace(".", " ")
                .replace("'", " ")
                .replace("@", " ")
        )
    }

    private fun disconnectIllegalName(packet: ConnectedPacket?, message: String) {
        server.logInfo(
            "{} was disconnected for illegal name. Name: {} Reason: {} ID {}",
            clientConnection,
            packet!!.name,
            message,
            clientConnection.uuid.mostSignificantBits
        )
        clientConnection.sendPacket(
            IllegalConnectionPacket("You have been disconnected for an illegal name. Name: " + packet.name + " Reason: " + message).transport()
        )
        clientConnection.close()
    }
}