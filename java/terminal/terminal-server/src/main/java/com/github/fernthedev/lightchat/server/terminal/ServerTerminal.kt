package com.github.fernthedev.lightchat.server.terminal

import com.github.fernthedev.config.common.Config
import com.github.fernthedev.config.common.exceptions.ConfigLoadException
import com.github.fernthedev.fernutils.console.ArgumentArrayUtils.parseArguments
import com.github.fernthedev.lightchat.core.StaticHandler.core
import com.github.fernthedev.lightchat.core.StaticHandler.setCore
import com.github.fernthedev.lightchat.core.StaticHandler.setDebug
import com.github.fernthedev.lightchat.core.encryption.transport
import com.github.fernthedev.lightchat.server.Console
import com.github.fernthedev.lightchat.server.SenderInterface
import com.github.fernthedev.lightchat.server.Server
import com.github.fernthedev.lightchat.server.settings.ServerSettings
import com.github.fernthedev.lightchat.server.terminal.backend.AutoCompleteHandler
import com.github.fernthedev.lightchat.server.terminal.backend.TabCompleteFinder
import com.github.fernthedev.lightchat.server.terminal.command.AuthTerminalHandler
import com.github.fernthedev.lightchat.server.terminal.command.Command
import com.github.fernthedev.lightchat.server.terminal.command.SettingsCommand
import com.github.fernthedev.terminal.core.CommonUtil.initTerminal
import com.github.fernthedev.terminal.core.CommonUtil.registerTerminalPackets
import com.github.fernthedev.terminal.core.CommonUtil.startSelfInCmd
import com.github.fernthedev.terminal.core.ConsoleHandler.Companion.startConsoleHandlerAsync
import com.github.fernthedev.terminal.core.TermCore
import com.github.fernthedev.terminal.core.packets.MessagePacket
import org.slf4j.LoggerFactory
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

object ServerTerminal {
    lateinit var settingsManager: Config<out ServerSettings>
    lateinit var commandHandler: ServerCommandHandler
    lateinit var server: Server

    lateinit var commandMessageParser: CommandMessageParser
    lateinit var autoCompleteHandler: TabCompleteFinder
    private val commandList: MutableList<Command> = ArrayList()

    val logger = LoggerFactory.getLogger(ServerTerminal::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        val port = AtomicInteger(-1)
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
            .handle("-debug") { queue: Queue<String> -> setDebug(true) }
            .apply()
        init(
            args,
            ServerTerminalSettings.builder()
                .port(port.get())
                .build()
        )
        startBind()
    }

    fun init(terminalSettings: ServerTerminalSettings) {
        init(arrayOf(), terminalSettings)
    }

    fun init(args: Array<String>, terminalSettings: ServerTerminalSettings) {
        initTerminal()


        //  Logger.getLogger("io.netty").setLevel(java.util.logging.Level.OFF);
        var port = terminalSettings.port
        if (terminalSettings.isLaunchConsoleInCMDWhenNone) startSelfInCmd(args)
        try {
            settingsManager = terminalSettings.serverSettings
            settingsManager.load()
            settingsManager.save()
        } catch (e: ConfigLoadException) {
            e.printStackTrace()
        }
        if (port == -1) port = settingsManager.configData.port

        server = Server(port)
        server.addPacketHandler(TerminalPacketHandler(server))
        server.settingsManager = settingsManager

        commandHandler = ServerCommandHandler(server)
        setCore(ServerTermCore(server), true)
        if (terminalSettings.isAllowTermPackets) registerTerminalPackets()
        if (terminalSettings.isConsoleCommandHandler) {
            server.startupLock.thenRun {
                startConsoleHandlerAsync((core as TermCore), AutoCompleteHandler(server))
                logger.info("Command handler ready! Type Command: (try help)")
            }
        }

        commandMessageParser = CommandMessageParser(server)
        server.pluginManager.registerEvents(commandMessageParser)
        registerCommand(SettingsCommand(server))
        if (terminalSettings.isAllowChangePassword) registerCommand(AuthTerminalHandler("changepassword", server))
        autoCompleteHandler = TabCompleteFinder(server)
    }

    fun startBind() {
        // Run on startup
        server.startupLock.thenRunAsync {
            try {
                if (!server.bind()
                        .await(15, TimeUnit.SECONDS)
                ) server.logger.error("Unable to bind port. Took longer than 15 seconds")
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                e.printStackTrace()
            }
        }
        server.run()

    }

    fun registerCommand(command: Command): Command {
        commandList.add(command)
        return command
    }

    val commands: List<Command>
        get() = commandList
    val currentPath: String
        get() = Paths.get("").toAbsolutePath().toString()

    fun broadcast(message: String?) {
        server.logger.info(message)
        server.sendObjectToAllPlayers(MessagePacket(message!!))
    }

    fun sendMessage(senderInterface: SenderInterface?, message: String?) {
        if (senderInterface is Console) logger.info(message) else senderInterface!!.sendPacket(
            MessagePacket(
                message!!
            )
                .transport(true)
        )
    }

    fun getCommandList(): List<Command> {
        return commandList
    }
}