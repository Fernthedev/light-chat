package com.github.fernthedev.lightchat.server

import com.github.fernthedev.config.common.Config
import com.github.fernthedev.lightchat.core.*
import com.github.fernthedev.lightchat.core.api.APIUsage
import com.github.fernthedev.lightchat.core.api.plugin.PluginManager
import com.github.fernthedev.lightchat.core.codecs.Codecs
import com.github.fernthedev.lightchat.core.codecs.general.compression.CompressionAlgorithms.getCompressions
import com.github.fernthedev.lightchat.core.codecs.general.json.EncryptedJSONObjectDecoder
import com.github.fernthedev.lightchat.core.codecs.general.json.EncryptedJSONObjectEncoder
import com.github.fernthedev.lightchat.core.encryption.PacketTransporter
import com.github.fernthedev.lightchat.core.encryption.transport
import com.github.fernthedev.lightchat.core.encryption.util.RSAEncryptionUtil
import com.github.fernthedev.lightchat.core.packets.Packet
import com.github.fernthedev.lightchat.core.packets.SelfMessagePacket
import com.github.fernthedev.lightchat.server.api.IPacketHandler
import com.github.fernthedev.lightchat.server.event.ServerShutdownEvent
import com.github.fernthedev.lightchat.server.event.ServerStartupEvent
import com.github.fernthedev.lightchat.server.netty.KeyThread
import com.github.fernthedev.lightchat.server.netty.MulticastServer
import com.github.fernthedev.lightchat.server.netty.ProcessingHandler
import com.github.fernthedev.lightchat.server.security.AuthenticationManager
import com.github.fernthedev.lightchat.server.security.BanManager
import com.github.fernthedev.lightchat.server.settings.ServerSettings
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.kqueue.KQueue
import io.netty.channel.kqueue.KQueueEventLoopGroup
import io.netty.channel.kqueue.KQueueServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.LengthFieldPrepender
import io.netty.handler.codec.MessageToByteEncoder
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import lombok.*
import org.apache.commons.lang3.time.StopWatch
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import java.security.KeyPair
import java.util.*
import java.util.concurrent.*
import java.util.function.Supplier

class Server(val port: Int) : Runnable {
    var logger = LoggerFactory.getLogger(Server::class.java)

    @APIUsage
    var serverThread: Thread? = null
        private set

    var authenticationManager = AuthenticationManager(this)
    var banManager = BanManager(this)

    @Volatile
    private var running = false

    @Volatile
    private var shutdown = false
    private var isPortBind = false

    val console: Console = Console(this)

    val pluginManager = PluginManager()

    var settingsManager: Config<out ServerSettings> = NoFileConfig(ServerSettings())

    private var multicastServer: MulticastServer? = null
    private var bossGroup: EventLoopGroup? = null
    private var workerGroup: EventLoopGroup? = null

    val rsaKeyThread: KeyThread<KeyPair>
    private var processingHandler: ProcessingHandler? = null
    private val channelHandlers: MutableList<ChannelHandler> = ArrayList()


    var maxPacketId = StaticHandler.DEFAULT_PACKET_ID_MAX

    @Deprecated(
        """Might be replaced with {@link ServerShutdownEvent}
      Use event system {@link PluginManager#registerEvents(Listener)}"""
    )
    private val shutdownListeners: MutableList<Runnable> = ArrayList()

    val startupLock = CompletableFuture<Void?>()
    val playerHandler: PlayerHandler = PlayerHandler(this)

    private var bootstrap: ServerBootstrap? = null

    /**
     * The listener will be called when [.shutdownServer] is called
     *
     * @param runnable the listener
     */
    @APIUsage
    fun addShutdownListener(runnable: Runnable) {
        shutdownListeners.add(runnable)
    }

    /**
     * Add channel handlers for netty functionality
     */
    @APIUsage
    fun addChannelHandler(channelHandler: ChannelHandler) {
        channelHandlers.add(channelHandler)
    }

    /**
     * Custom packet handlers added by outside the main server code
     */
    val packetHandlers: MutableList<IPacketHandler> = ArrayList()

    init {
        StaticHandler.setCore(ServerCore(this), false)
        val rsaKeyPoolSize = 15
        val serverCondition = Supplier { running }
        rsaKeyThread = KeyThread(
            { RSAEncryptionUtil.generateKeyPairs(settingsManager.configData.rsaKeySize) },
            rsaKeyPoolSize,
            serverCondition
        )
    }

    @APIUsage
    fun addPacketHandler(iPacketHandler: IPacketHandler) {
        packetHandlers.add(iPacketHandler)
    }

    @APIUsage
    fun removePacketHandler(packetHandler: IPacketHandler) {
        packetHandlers.remove(packetHandler)
    }


    fun sendObjectToAllPlayers(packet: Packet) {
        sendObjectToAllPlayers(packet.transport())
    }

    fun sendObjectToAllPlayers(packet: PacketTransporter) {
        for (clientConnection in playerHandler.channelMap.values) {
            logger.debug("Sending to all {} to {}", packet.packet.packetName, clientConnection)
            clientConnection.sendObject(packet)
        }
    }

    @lombok.Synchronized
    fun shutdownServer() {
        if (shutdown) throw IllegalStateException("Server is already shutting down!")

        shutdown = true
        logger.info(ColorCode.RED.toString() + "Shutting down server.")
        running = false

        multicastServer?.stopMulticast()
        if (workerGroup != null) workerGroup!!.shutdownGracefully()
        if (bossGroup != null) bossGroup!!.shutdownGracefully()
        pluginManager.callEvent(ServerShutdownEvent())
        shutdownListeners.forEach { obj: Runnable -> obj.run() }
    }

    fun isRunning(): Boolean {
        return running && serverThread!!.isAlive
    }

    /**
     * When an object implementing interface `Runnable` is used
     * to create a thread, starting the thread causes the object's
     * `run` method to be called in that separately executing
     * thread.
     *
     *
     * The general contract of the method `run` is that it may
     * take any action whatsoever.
     *
     * @see Thread.run
     */
    override fun run() {
        serverThread = Thread.currentThread()
        runBlocking {
            start()
        }
    }

    /**
     * Starts the server in the current thread
     */
    @APIUsage
    suspend fun start() = coroutineScope {

        check(!running) { "Server is already running" }
        running = true
        shutdown = false
        StaticHandler.displayVersion()
        val tasks: MutableList<Job> = ArrayList()
        val stopWatch = StopWatch()
        stopWatch.start()
        tasks.add(launch {
            Runtime.getRuntime().addShutdownHook(Thread {
                for (clientConnection in playerHandler.channelMap.values) {
                    if (clientConnection.channel.isOpen) {
                        logger.info("Gracefully shutting down")
                        sendObjectToAllPlayers(SelfMessagePacket(SelfMessagePacket.MessageType.LOST_SERVER_CONNECTION))
                        clientConnection.close()
                    }
                }
            })
        })


        /*
    try {
        new MulticastServer("Multicast",this).start();
    } catch (IOException e) {
        e.printStackTrace();
    }*/if (settingsManager.configData.useMulticast) {
        tasks.add(launch {
            logger.info("Initializing MultiCast Server")
            try {
                multicastServer = MulticastServer("MultiCast Thread", this@Server, StaticHandler.multicastAddress)
                multicastServer!!.start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        })
    }
        tasks.add(launch {
            authenticationManager = AuthenticationManager(this@Server)
            pluginManager.registerEvents(authenticationManager)
        })


//        LoggerManager loggerManager = new LoggerManager();
//        pluginManager.registerEvents(loggerManager, new ServerPlugin());
        logger.info("Running on [{}]", StaticHandler.OS)


//        await();

        tasks.add(launch {
            initServer()
        })
        for (taskInfo in tasks) {
            taskInfo.join()
        }

        logger.info("Finished initializing. Took {}ms", stopWatch.getTime(TimeUnit.MILLISECONDS))
        launch {
            pluginManager.callEvent(ServerStartupEvent(true))
        }
        startupLock.complete(null)

        launch {
            playerHandler.run()
        }

        while (running) {
            if (bossGroup!!.isShutdown || workerGroup!!.isShutdown) {
                running = false
                continue
            }
        }
        try {
            shutdownServer()
        } catch (ignored: IllegalStateException) {
        }
    }

    private fun initServer(): Server {
        bossGroup = NioEventLoopGroup()
        processingHandler = ProcessingHandler(this)
        workerGroup = NioEventLoopGroup()
        var channelClass: Class<out ServerChannel?> = NioServerSocketChannel::class.java
        if (settingsManager.configData.useNativeTransport) {
            logger.debug("Attempting to use native transport if available.")
            if (Epoll.isAvailable() /*|| SystemUtils.IS_OS_LINUX*/) {
                bossGroup = EpollEventLoopGroup()
                workerGroup = EpollEventLoopGroup()
                channelClass = EpollServerSocketChannel::class.java
                logger.info(ColorCode.GOLD.toString() + "OS IS LINUX! USING EPOLL TRANSPORT")
            }
            if (KQueue.isAvailable() /*|| SystemUtils.IS_OS_MAC_OSX || SystemUtils.IS_OS_FREE_BSD || SystemUtils.IS_OS_NET_BSD || SystemUtils.IS_OS_OPEN_BSD*/) {
                bossGroup = KQueueEventLoopGroup()
                workerGroup = KQueueEventLoopGroup()
                channelClass = KQueueServerSocketChannel::class.java
                logger.info(ColorCode.GOLD.toString() + "OS IS MAC/BSD! USING KQUEUE TRANSPORT")
            }
        }
        bootstrap = ServerBootstrap()
        val keyFinder = EncryptionKeyFinder(this)
        val jsonHandler = Codecs.getJsonHandler(settingsManager.configData.codec)
            ?: throw IllegalStateException("The codec " + settingsManager.configData.codec + " was not recognized")

        // Start key thread before initializing netty

        logger.info("Started RSA Key thread pool. Currently {} keys", rsaKeyThread.keysInPool)
        bootstrap!!.group(bossGroup, workerGroup)
            .channel(channelClass)
            .childHandler(object : ChannelInitializer<Channel>() {
                public override fun initChannel(ch: Channel) {

                    // inbound -> up to bottom
                    // outbound -> bottom to up
                    ch.pipeline().addLast(
                        LengthFieldBasedFrameDecoder(Int.MAX_VALUE, 0, 8, 0, 8)
                    )
                    ch.pipeline().addLast("strDecoder", StringDecoder(settingsManager.configData.charset))
                    ch.pipeline().addLast(
                        EncryptedJSONObjectDecoder(keyFinder, jsonHandler),
                        LengthFieldPrepender(8)
                    )
                    ch.pipeline().addLast("strEncoder", StringEncoder())
                    ch.pipeline()
                        .addLast(EncryptedJSONObjectEncoder(keyFinder, jsonHandler))
                    ch.pipeline().addLast(
                        "handler",
                        processingHandler
                    )
                    ch.pipeline().addLast(*channelHandlers.toTypedArray())
                    val compression: Int = settingsManager.configData.compressionLevel
                    val compressionAlgorithm: String = settingsManager.configData.compressionAlgorithm
                    val compressions = getCompressions(compressionAlgorithm)
                    var encoder: MessageToByteEncoder<*>? = null
                    val decoder = compressions.second.apply(settingsManager.configData)
                    if (compression >= 0) {
                        encoder =
                            compressions.first.apply(settingsManager.configData)
                    }

                    if (compressionAlgorithm != "NONE") {
                        if (decoder != null) {
                            ch.pipeline().addBefore("strDecoder", "compressDecoder", decoder)
                        }
                        if (compression >= 0 && encoder != null) {
                            ch.pipeline().addBefore("strEncoder", "compressEncoder", encoder)
                            logger.info("Using {} {} compression", compressionAlgorithm, compression)
                        }
                    }
                }
            })
            .option(ChannelOption.SO_BACKLOG, 128)
            .option(
                ChannelOption.RCVBUF_ALLOCATOR,
                AdaptiveRecvByteBufAllocator(512, 512, 64 * 1024)
            ) //       .option(EpollChannelOption.MAX_DATAGRAM_PAYLOAD_SIZE, 512)
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .childOption(ChannelOption.TCP_NODELAY, true)
        //                .childOption(ChannelOption.SO_TIMEOUT, 5000);
        logger.info("Server socket registered")
        return this
    }

    fun bind(): ChannelFuture {
        check(!isPortBind) { "Port is already set to bind on port $port" }
        return bootstrap!!.bind(port)
            .addListener(ChannelFutureListener { future: ChannelFuture ->
                isPortBind = if (future.isSuccess) {
                    try {
                        logger.info(
                            "Server started successfully at localhost (Connect with {}) using port {}",
                            InetAddress.getLocalHost().hostAddress,
                            port
                        )
                    } catch (e: UnknownHostException) {
                        logger.error(e.message, e)
                    }
                    logger.info("Bind port on {}", future.channel().localAddress())
                    true
                } else {
                    logger.info("Failed to bind port")
                    false
                }
            })
    }

    fun logInfo(o: String, vararg os: Any) {
        val objects: MutableList<Any> = ArrayList()
        objects.add(name)
        objects.addAll(listOf(*os))
        logger.info("[{}] $o", *objects.toTypedArray())
    }

    val name: String
        get() = serverThread!!.name
}