package com.github.fernthedev.lightchat.client

import com.github.fernthedev.lightchat.client.event.ServerConnectFinishEvent
import com.github.fernthedev.lightchat.client.event.ServerConnectHandshakeEvent
import com.github.fernthedev.lightchat.client.event.ServerDisconnectEvent.DisconnectStatus
import com.github.fernthedev.lightchat.core.StaticHandler
import com.github.fernthedev.lightchat.core.StaticHandler.VERSION_DATA
import com.github.fernthedev.lightchat.core.StaticHandler.getVersionRangeStatus
import com.github.fernthedev.lightchat.core.encryption.transport
import com.github.fernthedev.lightchat.core.exceptions.ParsePacketException
import com.github.fernthedev.lightchat.core.packets.IllegalConnectionPacket
import com.github.fernthedev.lightchat.core.packets.Packet
import com.github.fernthedev.lightchat.core.packets.SelfMessagePacket
import com.github.fernthedev.lightchat.core.packets.handshake.InitialHandshakePacket
import com.github.fernthedev.lightchat.core.packets.handshake.KeyResponsePacket
import com.github.fernthedev.lightchat.core.packets.handshake.RequestConnectInfoPacket
import com.github.fernthedev.lightchat.core.packets.latency.PingPacket
import com.github.fernthedev.lightchat.core.packets.latency.PingReceive
import com.github.fernthedev.lightchat.core.packets.latency.PongPacket
import com.github.fernthedev.lightchat.core.util.ExceptionUtil.throwParsePacketException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.security.InvalidKeyException
import java.util.concurrent.TimeUnit

class EventListener(private var client: Client) {
    suspend fun received(p: Packet, packetId: Int) = coroutineScope{
        try {
            when (p) {
                is PingPacket -> {
                    client.startPingStopwatch()
                    client.sendObject(PongPacket().transport(false))
                }

                is PingReceive -> {
                    client.endPingStopwatch()
                    client.logger.debug("Ping: {}", client.getPingTime(TimeUnit.MILLISECONDS).toString() + "ms")
                }

                is IllegalConnectionPacket -> {
                    client.logger.info(p.message)
                    client.disconnect(DisconnectStatus.CONNECTION_LOST)
                }

                is InitialHandshakePacket -> {
                    // Handles object encryption key sharing
                    val versionData = p.getVersionData()
                    val versionRange = getVersionRangeStatus(versionData)
                    if (versionRange === StaticHandler.VersionRange.MATCH_REQUIREMENTS) // Current version is smaller than the server's required minimum

                    // Current version is larger than server's minimum version
                    {
                        client.logger
                            .info("Version range requirements match Server version.")
                    } else {
                        // Current version is smaller than the server's required minimum
                        if (versionRange === StaticHandler.VersionRange.WE_ARE_LOWER) {
                            client.logger.info(
                                "The client version ({}) does not meet server's minimum version ({}) requirements. Expect incompatibility issues",
                                VERSION_DATA.version,
                                versionData.minVersion
                            )
                        }

                        // Current version is larger than server's minimum version
                        if (versionRange === StaticHandler.VersionRange.WE_ARE_HIGHER) {
                            client.logger.info(
                                "The server version ({}) does not meet client's minimum version ({}) requirements. Expect incompatibility issues",
                                versionData.version,
                                VERSION_DATA.minVersion
                            )
                        }
                    }
                    launch {
                        val secretKey = client.secretKey!!.await()
                        try {
                            val responsePacket = KeyResponsePacket(secretKey, p.getPublicKey())
                            client.sendObject(responsePacket.transport(false))
                        } catch (e: InvalidKeyException) {
                            e.printStackTrace()
                        }
                    }

                    client.eventHandler.callEvent(ServerConnectHandshakeEvent(client.channel!!))
                }

                is RequestConnectInfoPacket -> {
                    val connectedPacket = client.buildConnectedPacket()
                    client.sendObject(connectedPacket)
                    client.logger.info("Sent the connection Packet for request")
                }

                is SelfMessagePacket -> {
                    when (p.type) {
                        SelfMessagePacket.MessageType.TIMED_OUT_REGISTRATION -> {
                            client.logger.info("Timed out on registering.")
                            client.disconnect(DisconnectStatus.TIMEOUT)
                        }

                        SelfMessagePacket.MessageType.REGISTER_PACKET -> {
                            client.isRegistered = true
                            client.logger.info("Successfully connected to server")
                            client.eventHandler.callEvent(ServerConnectFinishEvent(client.channel!!))
                        }

                        SelfMessagePacket.MessageType.LOST_SERVER_CONNECTION -> {
                            client.logger.info("Lost connection to server! Must have shutdown!")
                            client.disconnect(DisconnectStatus.CONNECTION_LOST)
                        }

                        else -> {}
                    }
                }
            }

            client.packetHandlers.forEach { packetHandler ->
                try {
                    packetHandler.handlePacket(p, packetId)
                } catch (e: Exception) {
                    throw throwParsePacketException(e, p)
                }
            }
        } catch (e: ParsePacketException) {
            throw e
        } catch (e: Exception) {
            throw throwParsePacketException(e, p)
        }
    }
}