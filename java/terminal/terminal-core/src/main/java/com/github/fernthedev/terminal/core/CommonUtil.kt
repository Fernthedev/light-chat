package com.github.fernthedev.terminal.core

import com.github.fernthedev.lightchat.core.PacketJsonRegistry.registerPacketPackageFromClass
import com.github.fernthedev.lightchat.core.StaticHandler.isDebug
import com.github.fernthedev.lightchat.core.StaticHandler.setupLoggers
import com.github.fernthedev.terminal.core.packets.MessagePacket
import org.apache.commons.lang3.SystemUtils
import org.fusesource.jansi.AnsiConsole
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.system.exitProcess

object CommonUtil {
    @JvmStatic
    fun registerTerminalPackets() {
        registerPacketPackageFromClass(MessagePacket::class.java)
    }

    @JvmStatic
    fun initTerminal() {
        AnsiConsole.systemInstall()
        Logger.getLogger("io.netty").level = Level.OFF
        setupLoggers()
    }

    @JvmStatic
    fun startSelfInCmd(args: Array<String>) {
        if (System.console() == null && !isDebug() && SystemUtils.IS_OS_WINDOWS) {
            val filename: String?
            try {
                filename = File(CommonUtil::class.java.protectionDomain.codeSource.location.toURI()).path
            } catch (e: URISyntaxException) {
                e.printStackTrace()
                exitProcess(1)
            }
            System.err.println("No console found. Starting with CMD assuming it's Windows")
            val newArgs = arrayOf("cmd", "/c", "start", "cmd", "/c", "java -jar \"$filename\" -Xmx2G -Xms2G")
            val launchArgs: MutableList<String> = ArrayList(listOf(*newArgs))
            launchArgs.addAll(listOf(*args))
            try {
                Runtime.getRuntime().exec(launchArgs.toTypedArray())
                exitProcess(0)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}