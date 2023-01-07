package com.github.fernthedev.lightchat.server.security

import com.github.fernthedev.config.common.Config
import com.github.fernthedev.config.common.exceptions.ConfigLoadException
import com.github.fernthedev.config.gson.GsonConfig
import com.github.fernthedev.lightchat.server.*
import com.github.fernthedev.lightchat.server.event.BanEvent
import io.netty.channel.*
import lombok.*
import org.apache.commons.lang3.SystemUtils
import java.io.File

class BanManager(private val server: Server) {
    private var bannedDataConfig: Config<out BannedData>? = null

    init {
        try {
            initConfig()
        } catch (e: ConfigLoadException) {
            e.printStackTrace()
        }
    }

    @Throws(ConfigLoadException::class)
    protected fun initConfig() {
        bannedDataConfig = GsonConfig(BannedData(), bansFile)
        (bannedDataConfig as GsonConfig<*>).load()
    }

    fun isBanned(clientConnection: ClientConnection): Boolean {
        return isBanned(clientConnection.address)
    }

    fun isBanned(ip: String?): Boolean {
        for (bannedData in bannedDataConfig!!.configData.ipAddresses) {
            if (bannedData.equals(ip, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    fun ban(ip: String) {
        val banEvent = BanEvent(true, ip)
        server.pluginManager.callEvent(banEvent)
        if (banEvent.isCancelled) return
        bannedDataConfig!!.configData.ipAddresses.add(banEvent.bannedIP)
        try {
            bannedDataConfig!!.syncSave()
        } catch (e: ConfigLoadException) {
            e.printStackTrace()
        }
        closeAllIPs(ip)
    }

    fun unban(ip: String) {
        val banEvent = BanEvent(false, ip)
        server.pluginManager.callEvent(banEvent)
        if (banEvent.isCancelled) return
        bannedDataConfig!!.configData.ipAddresses.remove(banEvent.bannedIP)
        try {
            bannedDataConfig!!.syncSave()
        } catch (e: ConfigLoadException) {
            e.printStackTrace()
        }
    }

    protected fun closeAllIPs(ip: String?) {
        server.playerHandler.channelMap.forEach { (_: Channel?, clientConnection: ClientConnection) ->
            if (clientConnection.address.equals(
                    ip,
                    ignoreCase = true
                )
            ) clientConnection.close()
        }
    }


    data class BannedData(
        val ipAddresses: MutableList<String> = ArrayList()
    )

    companion object {
        private val bansFile = File(currentPath, "banned.json")
        private val currentPath: File
            private get() = SystemUtils.getUserDir()
    }
}