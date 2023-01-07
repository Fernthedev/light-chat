package com.github.fernthedev.lightchat.server

import com.github.fernthedev.lightchat.core.VersionData
import com.github.fernthedev.lightchat.core.api.APIUsage
import com.github.fernthedev.lightchat.core.encryption.*
import com.github.fernthedev.lightchat.core.encryption.util.EncryptionUtil
import com.github.fernthedev.lightchat.core.packets.Packet
import com.github.fernthedev.lightchat.core.packets.latency.PingPacket
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.apache.commons.lang3.time.StopWatch
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.Pair
import java.net.InetSocketAddress
import java.security.KeyPair
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import javax.crypto.Cipher
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey

/**
 * Handles client data
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ClientConnection(
    private val server: Server,
    val channel: Channel,
    val uuid: UUID,
    callback: Consumer<ClientConnection>
) : SenderInterface, AutoCloseable {
    var connected = false
        private set

    var registered = false

    val eventListener: EventListener

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
            assert(value != null)
            field = value
            tempKeyPair = null
            try {
                secureRandom = EncryptionUtil.getSecureRandom(value!!)
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
        }

    lateinit var encryptCipher: Cipher
        private set


    lateinit var decryptCipher: Cipher
        private set

    lateinit var secureRandom: SecureRandom
        private set

    fun encryptionRegistered(): Boolean {
        return this::secureRandom.isInitialized
    }

    /**
     * Packet:[ID,lastPacketSentTime]
     */
    private val packetIdMap: MutableMap<Class<out Packet>, Pair<Int, Long>> = HashMap()
    private val pingStopWatch = StopWatch()


    init {
        val keyJob = server.rsaKeyThread.randomKey
        keyJob.invokeOnCompletion {
            tempKeyPair = keyJob.getCompleted()
            callback.accept(this)
        }

        try {
            encryptCipher = EncryptionUtil.encryptCipher
            decryptCipher = EncryptionUtil.decryptCipher
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        eventListener = EventListener(server, this)
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

    private fun updatePacketIdPair(packet: Class<out Packet>, newId: Int): Pair<Int, Long> {
        var copiedId = newId
        var packetIdPair = packetIdMap[packet]
        if (packetIdPair == null) {
            packetIdPair = ImmutablePair(0, System.currentTimeMillis())
        } else {
            if (copiedId == -1) {
                copiedId = packetIdPair.key + 1
            }
            packetIdPair = ImmutablePair(copiedId, System.currentTimeMillis())
        }
        packetIdMap[packet] = packetIdPair
        return packetIdPair
    }

    /**
     *
     * @param packet Packet to send
     * @param encrypt if true the packet will be encrypted
     */
    @APIUsage
    @Deprecated("Use packet wrapper", ReplaceWith(
        "sendObject(packet.transport(encrypt))",
        "com.github.fernthedev.lightchat.core.encryption.transport"
    )
    )
    fun sendObject(packet: Packet, encrypt: Boolean): ChannelFuture {
        return sendObject(packet.transport(encrypt))
    }

    fun sendObject(transporter: PacketTransporter): ChannelFuture {
        val packet = transporter.packet
        val packetIdPair = updatePacketIdPair(packet.javaClass, -1)
        if (packetIdPair.left > server.maxPacketId || System.currentTimeMillis() - packetIdPair.right > 900) {
            updatePacketIdPair(
                packet.javaClass,
                0
            )
        }
        return channel.writeAndFlush(transporter)
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


        //serverSocket.close();
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
    fun ping() {
        pingStopWatch.reset()
        pingStopWatch.start()
        sendObject(PingPacket().transport(false))
    }

    override fun sendPacket(packet: Packet): ChannelFuture {
        return sendObject(packet.transport())
    }

    override fun sendPacket(packet: PacketTransporter): ChannelFuture {
        return sendObject(packet)
    }

    fun finishPing() {
        if (!pingStopWatch.isStopped) pingStopWatch.stop()
    }

    /**
     * Packet:[ID,lastPacketSentTime]
     */
    fun getPacketId(packet: Class<out Packet>): Pair<Int, Long> {
        packetIdMap.computeIfAbsent(packet) { ImmutablePair(0, -1L) }
        return packetIdMap[packet]!!
    }

    fun finishConstruct(name: String, os: String, langFramework: String) {
        this.name = name
        this.os = os
        this.langFramework = langFramework
    }
}