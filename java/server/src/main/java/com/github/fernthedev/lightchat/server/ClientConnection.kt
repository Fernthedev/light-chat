package com.github.fernthedev.lightchat.server

import com.github.fernthedev.lightchat.core.VersionData
import com.github.fernthedev.lightchat.core.api.APIUsage
import com.github.fernthedev.lightchat.core.codecs.AcceptablePacketTypes
import com.github.fernthedev.lightchat.core.encryption.*
import com.github.fernthedev.lightchat.core.encryption.util.EncryptionUtil
import com.github.fernthedev.lightchat.core.packets.latency.PingPacket
import io.netty.channel.Channel
import kotlinx.coroutines.*
import org.apache.commons.lang3.time.StopWatch
import java.net.InetSocketAddress
import java.security.KeyPair
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.SecretKey

/**
 * Handles client data
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ClientConnection(
    private val server: Server,
    val channel: Channel,
    val uuid: UUID
) : SenderInterface, AutoCloseable {
    var connected = false
        private set

    var registered = false

    val eventListener: EventListener = EventListener(server, this)

    lateinit var os: String
        private set

    lateinit var langFramework: String
        private set


    lateinit var versionData: VersionData


    override lateinit var name: String
        private set

    /**
     * The keypair encryption
     * Used in initial connection establishment
     *
     * Used before secret key is initialized
     */
    var tempKeyPair: KeyPair? = null
        private set

    var secretKey: SecretKey? = null
        set(value) {
            requireNotNull(value)
            field = value
            tempKeyPair = null
            secureRandom = EncryptionUtil.getSecureRandom(value)
        }

    var encryptCipher: ThreadLocal<Cipher>
        private set


    var decryptCipher: ThreadLocal<Cipher>
        private set

    var secureRandom: SecureRandom? = null
        private set

    fun encryptionRegistered(): Boolean {
        return secretKey != null
    }

    /**
     * Packet:[ID,lastPacketSentTime]
     */
    private val packetJSONIdMap: MutableMap<Class<out AcceptablePacketTypes>, Pair<Int, Long>> = HashMap()
    private val pingStopWatch = StopWatch()


    init {
        encryptCipher = ThreadLocal.withInitial { EncryptionUtil.generateEncryptCipher() }
        decryptCipher = ThreadLocal.withInitial { EncryptionUtil.generateDecryptCipher() }
    }

    /**
     *
     * @param timeUnit the time to use
     * @return ping delay of client
     */
    @APIUsage
    fun getPingDelay(timeUnit: TimeUnit?): Long {
        return pingStopWatch.getTime(timeUnit)
    }

    private fun updatePacketIdPair(packetJSON: Class<out AcceptablePacketTypes>, newId: Int): Pair<Int, Long> {
        var copiedId = newId
        var packetIdPair = packetJSONIdMap[packetJSON]
        if (packetIdPair == null) {
            packetIdPair = Pair(0, System.currentTimeMillis())
        } else {
            if (copiedId == -1) {
                copiedId = packetIdPair.first + 1
            }
            packetIdPair = Pair(copiedId, System.currentTimeMillis())
        }
        packetJSONIdMap[packetJSON] = packetIdPair
        return packetIdPair
    }


    /**
     * Closes connection
     */
    override fun close() {

        //DISCONNECT FROM SERVER
        server.logger.info("Closing player {}", this)
        channel.close()
        server.playerHandler.channelMap.remove(channel)
        connected = false
        server.playerHandler.uuidMap.remove(uuid)
    }

    override fun toString(): String {
        return "[$address|$name]"
    }

    val address: String?
        /**
         *
         * @return the client address
         */
        get() {
            if (channel.remoteAddress() == null) {
                return null
            }
            val address = channel.remoteAddress() as InetSocketAddress
            return address.address.toString()
        }

    /**
     * Pings player
     */
    suspend fun ping() {
        pingStopWatch.reset()
        pingStopWatch.start()
        sendPacketLaunch(PingPacket().transport(false))
    }

    override suspend fun sendPacketDeferred(transporter: PacketTransporter) = coroutineScope {
        val packet = transporter.packet
        val packetIdPair = updatePacketIdPair(packet.javaClass, -1)
        if (packetIdPair.first > server.maxPacketId || System.currentTimeMillis() - packetIdPair.second > 900) {
            updatePacketIdPair(
                packet.javaClass,
                0
            )
        }
        return@coroutineScope async(Dispatchers.IO) {
            return@async channel.writeAndFlush(transporter)
        }
    }

    override suspend fun sendPacketLaunch(transporter: PacketTransporter): Job = coroutineScope {
        val packet = transporter.packet
        val packetIdPair = updatePacketIdPair(packet.javaClass, -1)
        if (packetIdPair.first > server.maxPacketId || System.currentTimeMillis() - packetIdPair.second > 900) {
            updatePacketIdPair(
                packet.javaClass,
                0
            )
        }
        return@coroutineScope launch(Dispatchers.IO) {
            channel.writeAndFlush(transporter)
        }
    }

    fun finishPing() {
        if (!pingStopWatch.isStopped) pingStopWatch.stop()
    }

    /**
     * Packet:[ID,lastPacketSentTime]
     */
    fun getPacketId(packetJSON: Class<out AcceptablePacketTypes>): Pair<Int, Long> {
        packetJSONIdMap.computeIfAbsent(packetJSON) { Pair(0, -1L) }
        return packetJSONIdMap[packetJSON]!!
    }

    fun finishConstruct(name: String, os: String, langFramework: String) {
        this.name = name
        this.os = os
        this.langFramework = langFramework
    }

    internal fun setupKeypair(key: KeyPair) {
        tempKeyPair = key
    }
}