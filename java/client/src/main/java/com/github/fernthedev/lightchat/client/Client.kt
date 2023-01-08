package com.github.fernthedev.lightchat.client

import com.github.fernthedev.config.common.Config
import com.github.fernthedev.lightchat.client.api.IPacketHandler
import com.github.fernthedev.lightchat.client.event.ServerDisconnectEvent
import com.github.fernthedev.lightchat.client.netty.ClientHandler
import com.github.fernthedev.lightchat.core.NoFileConfig
import com.github.fernthedev.lightchat.core.StaticHandler
import com.github.fernthedev.lightchat.core.api.APIUsage
import com.github.fernthedev.lightchat.core.api.Async
import com.github.fernthedev.lightchat.core.api.plugin.PluginManager
import com.github.fernthedev.lightchat.core.codecs.Codecs
import com.github.fernthedev.lightchat.core.codecs.JSONHandler
import com.github.fernthedev.lightchat.core.codecs.general.compression.SnappyCompressor
import com.github.fernthedev.lightchat.core.codecs.general.json.EncryptedJSONObjectDecoder
import com.github.fernthedev.lightchat.core.codecs.general.json.EncryptedJSONObjectEncoder
import com.github.fernthedev.lightchat.core.encryption.PacketTransporter
import com.github.fernthedev.lightchat.core.encryption.RSA.IEncryptionKeyHolder
import com.github.fernthedev.lightchat.core.encryption.transport
import com.github.fernthedev.lightchat.core.encryption.util.EncryptionUtil
import com.github.fernthedev.lightchat.core.exceptions.DebugException
import com.github.fernthedev.lightchat.core.packets.Packet
import com.github.fernthedev.lightchat.core.packets.handshake.ConnectedPacket
import com.google.common.base.Stopwatch
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollSocketChannel
import io.netty.channel.kqueue.KQueueEventLoopGroup
import io.netty.channel.kqueue.KQueueSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.LengthFieldPrepender
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import kotlinx.coroutines.*
import org.apache.commons.lang3.SystemUtils
import org.apache.commons.lang3.time.StopWatch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.SecretKey

class Client(private var host: String, private var port: Int) : IEncryptionKeyHolder, AutoCloseable {
    var logger: Logger = LoggerFactory.getLogger(Client::class.java)

    @JvmField
    var clientSettingsManager: Config<out ClientSettings> = NoFileConfig(ClientSettings())

    protected var listener: EventListener
    var clientHandler: ClientHandler

    protected var future: ChannelFuture? = null

    var channel: Channel? = null
        protected set

    protected var workerGroup: EventLoopGroup? = null
    var isRegistered = false

    val pluginManager = PluginManager()
    var isRunning = false
    val packetHandlers: MutableList<IPacketHandler> = ArrayList()

    @APIUsage
    var name: String? = null

    /**
     * Is null until a connection is established
     */
    var secretKey: Deferred<SecretKey>? = null
        private set
    private val stopWatch: StopWatch = StopWatch()

    /**
     * Packet:[ID,lastPacketSentTime]
     */
    private val packetIdMap: MutableMap<Class<out Packet>, Pair<Int, Long>> = HashMap()
    var maxPacketId: Int = StaticHandler.DEFAULT_PACKET_ID_MAX

    fun buildConnectedPacket(): ConnectedPacket {
        StaticHandler.core.logger.debug("Using the name: {}", name)
        val javaVersion = System.getProperty("java.version") + " (" + SystemUtils.JAVA_VM_NAME + ")"
        return ConnectedPacket(name!!, oSName, StaticHandler.VERSION_DATA, "Java $javaVersion")
    }

    var decryptCipher: Cipher? = null
        private set
    var encryptCipher: Cipher? = null
        private set
    var secureRandom: SecureRandom? = null
        private set

    init {
        StaticHandler.setCore(ClientCore(this), false)
        listener = EventListener(this)
        clientHandler = ClientHandler(this, listener)
        if (name == null) {
            name = InetAddress.getLocalHost().hostName
        }
        initialize(host, port)
    }

    @APIUsage
    fun addPacketHandler(iPacketHandler: IPacketHandler) {
        packetHandlers.add(iPacketHandler)
    }

    @APIUsage
    fun removePacketHandler(packetHandler: IPacketHandler) {
        packetHandlers.remove(packetHandler)
    }

    fun setup() {
        isRunning = true
    }

    fun initialize(host: String, port: Int) {
        this.port = port
        this.host = host
        initialize()
    }

    fun initialize() {
        setup()
        logger.info("Initializing")
        StaticHandler.displayVersion()
    }

    val oSName: String
        get() = System.getProperty("os.name")

    fun connectBlocking(): ChannelFuture = runBlocking {
        return@runBlocking connect()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @APIUsage
    @Async
    @Throws(InterruptedException::class)
    suspend fun connect(): ChannelFuture = coroutineScope {
        isRegistered = false
        channel = null
        future = null
        workerGroup = null
        logger.info("Connecting to server.")
        val b = Bootstrap()
        workerGroup = NioEventLoopGroup()
        var channelClass: Class<out AbstractChannel?> = NioSocketChannel::class.java
        val clientSettings = clientSettingsManager.configData
        if (SystemUtils.IS_OS_LINUX && clientSettings.isRunNatives) {
            workerGroup = EpollEventLoopGroup()
            channelClass = EpollSocketChannel::class.java
        }
        if (SystemUtils.IS_OS_MAC_OSX && clientSettings.isRunNatives) {
            workerGroup = KQueueEventLoopGroup()
            channelClass = KQueueSocketChannel::class.java
        }

        val jsonHandler: JSONHandler = Codecs.getJsonHandler(clientSettings.codec)
            ?: throw IllegalStateException("The codec " + clientSettings.codec + " was not recognized")
        b.group(workerGroup)
        b.channel(channelClass)
        b.option(ChannelOption.SO_KEEPALIVE, true)
        b.option(ChannelOption.TCP_NODELAY, true)
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientSettings.timeoutTime.toInt())
        b.handler(object : ChannelInitializer<Channel>() {
            override fun initChannel(ch: Channel) {

                // inbound -> up to bottom
                // outbound -> bottom to up

                // Decoders
                ch.pipeline().addLast(
                    LengthFieldBasedFrameDecoder(Int.MAX_VALUE, 0, 8, 0, 8),
                    SnappyCompressor.snappyDecoder()
                )
                ch.pipeline().addLast("strDecoder", StringDecoder(clientSettings.charset))
                ch.pipeline().addLast(
                    EncryptedJSONObjectDecoder(this@Client, jsonHandler),
                )


                // Encoders
                ch.pipeline().addLast(
                    LengthFieldPrepender(8),
                    SnappyCompressor.snappyFrameEncoder()
                )
                ch.pipeline().addLast("strEncoder", StringEncoder())
                ch.pipeline().addLast(EncryptedJSONObjectEncoder(this@Client, jsonHandler))


                // Handlers
                ch.pipeline().addLast(
                    "handler",
                    clientHandler
                )
            }
        })
        logger.info("Establishing connection")
        val connectTime: Stopwatch = Stopwatch.createStarted()
        secretKey = async {
            return@async EncryptionUtil.generateSecretKey()
        }

        launch {
            val key = secretKey!!.await()
            encryptCipher = EncryptionUtil.encryptCipher
            decryptCipher = EncryptionUtil.decryptCipher
            secureRandom = EncryptionUtil.getSecureRandom(key)
        }




        future = b.connect(host, port)
        val future = future!!
        return@coroutineScope future.addListener {
            channel = future.channel()
            if (future.isSuccess && future.channel().isActive) {
                connectTime.stop()
                StaticHandler.core.logger.debug(
                    "Time taken to connect: {}ms",
                    connectTime.elapsed(TimeUnit.MILLISECONDS)
                )
                isRunning = true
            }
        }
    }

    private fun updatePacketIdPair(
        packet: Class<out Packet>,
        newId: Int
    ): Pair<Int, Long> {
        var newId = newId

        var packetIdPair = getPacketId(packet)

        if (newId == -1) newId = packetIdPair.first + 1
        packetIdPair = Pair(newId, System.currentTimeMillis())
        packetIdMap[packet] = packetIdPair
        return packetIdPair
    }

    /**
     *
     * @param packet Packet to send
     * @param encrypt if true the packet will be encrypted
     */
    @APIUsage
    @Deprecated(
        "Use packet wrapper", ReplaceWith(
            "sendObject(packet.transport(encrypt))",
            "com.github.fernthedev.lightchat.core.encryption.transport"
        )
    )
    fun sendObject(packet: Packet, encrypt: Boolean): ChannelFuture {
        return sendObject(packet.transport(encrypt))
    }

    @APIUsage
    fun sendObject(transporter: PacketTransporter): ChannelFuture {
        val packet = transporter.packet
        val packetIdPair = updatePacketIdPair(packet.javaClass, -1)
        if (packetIdPair.first > maxPacketId || System.currentTimeMillis() - packetIdPair.second > 900) {
            updatePacketIdPair(
                packet.javaClass,
                0
            )
        }
        return channel!!.writeAndFlush(transporter)
    }

    @APIUsage
    fun sendObject(packet: Packet?): ChannelFuture {
        return sendObject(packet!!, true)
    }

    @JvmOverloads
    fun disconnect(disconnectStatus: ServerDisconnectEvent.DisconnectStatus = ServerDisconnectEvent.DisconnectStatus.DISCONNECTED) {
        logger.info("Closing connection.")
        isRunning = false
        if (channel != null && channel!!.isActive) {
            //DISCONNECT FROM SERVER
            if (StaticHandler.isDebug()) {
                try {
                    throw DebugException()
                } catch (e: DebugException) {
                    logger.error("Debug stacktrace, not an actual error", e)
                }
            }
            if (channel!!.isActive) {
                channel!!.closeFuture()
                logger.info("Closed connection.")
            }
        }
        channel?.let {
            pluginManager.callEvent(ServerDisconnectEvent(it, disconnectStatus))
        }
        workerGroup?.shutdownGracefully()
        channel = null
        future = null
        workerGroup = null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getSecretKey(ctx: ChannelHandlerContext, channel: Channel): SecretKey {
        return secretKey!!.getCompleted()
    }

    override fun getEncryptCipher(ctx: ChannelHandlerContext, channel: Channel): Cipher {
        return encryptCipher!!
    }

    override fun getDecryptCipher(ctx: ChannelHandlerContext, channel: Channel): Cipher {
        return decryptCipher!!
    }

    override fun getSecureRandom(ctx: ChannelHandlerContext, channel: Channel): SecureRandom {
        return secureRandom!!
    }

    override fun isEncryptionKeyRegistered(ctx: ChannelHandlerContext, channel: Channel): Boolean {
        return secretKey != null
    }

    /**
     * Packet:[ID,lastPacketSentTime]
     */
    override fun getPacketId(
        clazz: Class<out Packet>,
        ctx: ChannelHandlerContext,
        channel: Channel
    ): Pair<Int, Long> {
        return getPacketId(clazz)
    }

    /**
     * Packet:[ID,lastPacketSentTime]
     */
    @APIUsage
    fun getPacketId(
        clazz: Class<out Packet>
    ): Pair<Int, Long> {
        packetIdMap.computeIfAbsent(
            clazz
        ) { aClass: Class<out Packet>? ->
            Pair(
                0,
                -1L
            )
        }
        return packetIdMap[clazz]!!
    }

    fun startPingStopwatch() {
        stopWatch.reset()
        stopWatch.start()
    }

    fun endPingStopwatch() {
        if (!stopWatch.isStopped) stopWatch.suspend()
    }

    fun getPingTime(timeUnit: TimeUnit?): Long {
        // Return -1 if ping has not been measured yet
        return if (!stopWatch.isStarted && !stopWatch.isSuspended) -1 else stopWatch.getTime(timeUnit)
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     *
     *  As noted in [AutoCloseable.close], cases where the
     * close may fail require careful attention. It is strongly advised
     * to relinquish the underlying resources and to internally
     * *mark* the `Closeable` as closed, prior to throwing
     * the `IOException`.
     */
    override fun close() {
        disconnect()
    }


}