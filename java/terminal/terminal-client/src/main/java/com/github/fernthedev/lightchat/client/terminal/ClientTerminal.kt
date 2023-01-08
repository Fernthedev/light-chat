package com.github.fernthedev.lightchat.client.terminal

import com.github.fernthedev.fernutils.console.ArgumentArrayUtils.parseArguments
import com.github.fernthedev.lightchat.client.Client
import com.github.fernthedev.lightchat.client.netty.MulticastClient
import com.github.fernthedev.lightchat.core.MulticastData
import com.github.fernthedev.lightchat.core.StaticHandler
import com.github.fernthedev.lightchat.core.StaticHandler.VERSION_DATA
import com.github.fernthedev.lightchat.core.StaticHandler.core
import com.github.fernthedev.lightchat.core.StaticHandler.getVersionRangeStatus
import com.github.fernthedev.lightchat.core.StaticHandler.setCore
import com.github.fernthedev.lightchat.core.StaticHandler.setDebug
import com.github.fernthedev.lightchat.core.VersionData
import com.github.fernthedev.terminal.core.CommonUtil.initTerminal
import com.github.fernthedev.terminal.core.CommonUtil.registerTerminalPackets
import com.github.fernthedev.terminal.core.CommonUtil.startSelfInCmd
import com.github.fernthedev.terminal.core.ConsoleHandler.Companion.startConsoleHandlerAsync
import com.github.fernthedev.terminal.core.TermCore
import com.github.fernthedev.terminal.core.packets.CommandPacket
import com.github.fernthedev.terminal.core.packets.MessagePacket
import com.google.common.base.Stopwatch
import lombok.SneakyThrows
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.Pair
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

object ClientTerminal {
    var logger = LoggerFactory.getLogger(ClientTerminal::class.java)
        internal set
    lateinit var autoCompleteHandler: AutoCompleteHandler
        private set

    internal lateinit var client: Client
    internal var clientSupplier: (host: String, port: Int) -> Client =
        { host: String, port: Int -> Client(host, port) }


    val messageDelay = Stopwatch.createUnstarted()

    @JvmStatic
    fun main(args: Array<String>) {
        val port = AtomicInteger(-1)
        val host = AtomicReference<String?>(null)
        parseArguments(args)
            .handle("-port") { queue: Queue<String> ->
                try {
                    port.set(queue.remove().toInt())
                    if (port.get() <= 0) {
                        logger.error("-port cannot be less than 0")
                        port.set(-1)
                    } else logger.info("Using port {}", port)
                } catch (e: NumberFormatException) {
                    logger.error("-port is not a number")
                    port.set(-1)
                }
            }
            .handle("-host") { queue: Queue<String?> ->
                try {
                    host.set(queue.remove())
                    logger.info("Using host {}", host.get())
                } catch (e: IndexOutOfBoundsException) {
                    logger.error("Cannot find argument for -host")
                    host.set(null)
                }
            }
            .handle("-debug") { setDebug(true) }
            .apply()
        init(
            args, ClientTerminalSettings.builder()
                .host(host.get())
                .port(port.get())
                .build()
        )
        connect()
    }

    fun init(settings: ClientTerminalSettings) {
        init(arrayOf(), settings)
    }

    @SneakyThrows
    fun init(args: Array<String>, settings: ClientTerminalSettings) {
        initTerminal()
        val host = AtomicReference(settings.host!!)
        val port = AtomicInteger(settings.port)
        if (settings.isLaunchConsoleInCMDWhenNone) {
            startSelfInCmd(args)
        }
        if (settings.isCheckForServersInMulticast) {
            val multicastClient: MulticastClient
            val scanner = Scanner(System.`in`)
            if (host.get() == null || host.get() == "" || port.get() == -1) {
                multicastClient = MulticastClient()
                try {
                    val hostPortPair = check(multicastClient, scanner, 4)
                    if (hostPortPair != null) {
                        host.set(hostPortPair.left)
                        port.set(hostPortPair.right)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        if (settings.isAskUserForHostPort) {
            val scanner = Scanner(System.`in`)
            while (host.get() == null || host.get().equals("", ignoreCase = true) || port.get() == -1) {
                if (host.get() == null || host.get() == "") {
                    host.set(readLine(scanner, "Host:"))
                }
                if (port.get() == -1) {
                    port.set(readInt(scanner, "Port:"))
                }
            }
        }
        check(!(host.get() == null || host.get() == "")) { "Host is null or not provided. Provide in settings or allow user to provide" }
        check(port.get() != -1) { "Port is null or not provided. Provide in settings or allow user to provide" }
        client = clientSupplier(host.get(), port.get())
        client.clientSettingsManager = settings.clientSettings
        client.clientSettingsManager.load()
        client.clientSettingsManager.save()
        setCore(ClientTermCore(client), true)
        if (settings.isAllowTermPackets) registerTerminalPackets()
        if (settings.isConsoleCommandHandler) {
            autoCompleteHandler = AutoCompleteHandler(
                client
            )
            startConsoleHandlerAsync((core as TermCore), autoCompleteHandler)
        }

        val packetHandler = PacketHandler()
        client.addPacketHandler(packetHandler)
        if (settings.isShutdownOnDisconnect) {
            client.pluginManager.registerEvents(packetHandler)
        }
    }

    fun connect() {
        client.connectBlocking()
    }

    private fun readLine(scanner: Scanner, message: String): String? {
        if (message != "") {
            logger.info(message)
        }
        return if (scanner.hasNextLine()) {
            scanner.nextLine()
        } else null
    }

    private fun readInt(scanner: Scanner, message: String): Int {
        if (message != "") {
            logger.info(message)
        }
        return if (scanner.hasNextLine()) {
            scanner.nextInt()
        } else -1
    }

    internal fun check(multicastClient: MulticastClient, scanner: Scanner, amount: Int): Pair<String?, Int>? {
        logger.info("Looking for MultiCast servers")
        multicastClient.checkServers(amount)
        var host: String? = null
        var port = -1
        if (multicastClient.getServersAddress().isNotEmpty()) {
            val servers: MutableMap<Int, MulticastData> = HashMap()
            logger.info("Select one of these servers, or use none to skip, refresh to refresh")
            var index = 0
            for (serverAddress in multicastClient.getServersAddress()) {
                index++
                servers[index] = serverAddress
                val serverCurrent = DefaultArtifactVersion(serverAddress.version)
                val serverMin = DefaultArtifactVersion(serverAddress.minVersion)
                val range = getVersionRangeStatus(VersionData(serverCurrent, serverMin))
                if (range === StaticHandler.VersionRange.MATCH_REQUIREMENTS) {
                    println(">" + index + " | " + serverAddress.address + ":" + serverAddress.port)
                } else {
                    // Current version is smaller than the server's required minimum
                    if (range === StaticHandler.VersionRange.WE_ARE_LOWER) {
                        println(">" + index + " | " + serverAddress.address + ":" + serverAddress.port + " (Server's required minimum version is " + serverAddress.minVersion + " while your current version is smaller {" + VERSION_DATA.version + "} Incompatibility issues may arise)")
                    }

                    // Current version is larger than server's minimum version
                    if (range === StaticHandler.VersionRange.WE_ARE_HIGHER) {
                        println(">" + index + " | " + serverAddress.address + ":" + serverAddress.port + " (Server's version is " + serverAddress.version + " while your minimum version is larger {" + VERSION_DATA.minVersion + "} Incompatibility issues may arise)")
                    }
                }
            }
            while (scanner.hasNextLine()) {
                var answer = scanner.nextLine()
                answer = answer.replace(" ".toRegex(), "")
                if (answer.matches("[0-9]+".toRegex())) {
                    try {
                        val serverIndex = answer.toInt()
                        if (servers.containsKey(serverIndex)) {
                            val serverAddress = servers[index]
                            host = serverAddress!!.address
                            port = serverAddress.port
                            logger.info("Selected {}:{}", serverAddress.address, serverAddress.port)
                            break
                        } else {
                            logger.info("Not in the list")
                        }
                    } catch (ignored: NumberFormatException) {
                        logger.info("Not a number or refresh/none")
                    }
                }
                when (answer) {
                    "none" -> return null
                    "refresh" -> return check(multicastClient, scanner, 7)
                    else -> logger.info("Unknown argument")
                }
            }
        }
        return ImmutablePair(host, port)
    }

    fun sendMessage(message: String) {
        var message = message
        try {
            message = message.replace(" {2}".toRegex(), " ")
            if (message != "" && message != " ") {
                if (message.startsWith("/")) {
                    client.sendObject(CommandPacket(message.substring(1)))
                } else {
                    client.sendObject(MessagePacket(message))
                }
            }
        } catch (e: IllegalArgumentException) {
            logger.error("Unable to send the message. Cause: {} {{}}", e.message, e.javaClass.name)
        }
    }
}