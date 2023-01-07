package com.github.fernthedev.lightchat.server

import com.github.fernthedev.lightchat.core.packets.SelfMessagePacket
import io.netty.channel.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles players
 */
class PlayerHandler(
    val server: Server
) {

    val channelMap: MutableMap<Channel, ClientConnection> = ConcurrentHashMap()
    val uuidMap: MutableMap<UUID, ClientConnection> = ConcurrentHashMap()

    private val clientPlayerMap: MutableMap<ClientConnection, TimeoutData> = ConcurrentHashMap()


    suspend fun run() = coroutineScope {
        while (server.isRunning()) {
            for (clientConnection in channelMap.values) {
                var timeoutData = clientPlayerMap[clientConnection]
                if (timeoutData == null) {
                    timeoutData = TimeoutData(0, 0)
                    clientPlayerMap[clientConnection] = timeoutData
                }

                timeoutData.secondsPassed++

                if (!clientConnection.registered || !clientConnection.channel.isActive) {
                    timeoutData.registerTimeout++
                } else {
                    timeoutData.registerTimeout =
                        0
                }

                if (timeoutData.registerTimeout >= server.settingsManager.configData.timeoutTime / 1000 && !clientConnection.registered) {
                    clientConnection.sendObject(
                        SelfMessagePacket(SelfMessagePacket.MessageType.TIMED_OUT_REGISTRATION),
                        false
                    )
                    clientConnection.close()
                }

                if (!clientConnection.channel.isActive && timeoutData.registerTimeout >= 30) {
                    clientConnection.close()
                }

                if (timeoutData.secondsPassed >= 5) {
                    clientConnection.ping()
                    timeoutData.secondsPassed = 0
                }
            }

            delay(1000)
        }
    }


    data class TimeoutData(
        var secondsPassed: Int = 0,
        var registerTimeout: Int = 0
    )
}