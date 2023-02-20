package com.github.fernthedev.lightchat.server.terminal.command

import com.github.fernthedev.config.common.exceptions.ConfigLoadException
import com.github.fernthedev.lightchat.core.ColorCode
import com.github.fernthedev.lightchat.server.SenderInterface
import com.github.fernthedev.lightchat.server.Server
import com.github.fernthedev.lightchat.server.terminal.ServerTerminal
import java.util.*

class SettingsCommand(private val server: Server) : Command("settings") {
    override suspend fun onCommand(sender: SenderInterface, args: Array<String>) {
        if (args.isEmpty()) {
            ServerTerminal.sendMessage(sender, "Possible args: set,get,reload,save, list")
        } else {
            val authenticated = server.authenticationManager.authenticate(sender).await()

            if (authenticated) {
                val timeStart: Long
                val timeEnd: Long
                val timeElapsed: Long
                val arg = args[0]
                val settingsManager = server.settingsManager
                when (arg.lowercase(Locale.getDefault())) {
                    "set" -> if (args.size > 2) {
                        val settingName = args[1]
                        val newValue = args[2]
                        try {
                            settingsManager.configData.setValue(settingName, newValue)
                            ServerTerminal.sendMessage(
                                sender, ColorCode.GREEN.toString() + "Set " + settingName + " to " + newValue
                            )
                            ServerTerminal.sendMessage(
                                sender,
                                ColorCode.YELLOW.toString() + "Some settings will require a restart to take effect."
                            )
                        } catch (e: ClassCastException) {
                            ServerTerminal.sendMessage(
                                sender, "Error: " + e.message + " {" + e.javaClass.name + "}"
                            )
                        } catch (e: IllegalArgumentException) {
                            ServerTerminal.sendMessage(
                                sender, "Error: " + e.message + " {" + e.javaClass.name + "}"
                            )
                        }
                    } else ServerTerminal.sendMessage(sender, "Usage: settings set {name} {newvalue}")

                    "get" -> if (args.size > 1) {
                        val key = args[1]
                        try {
                            val value = settingsManager.configData.getValue(key)
                            ServerTerminal.sendMessage(sender, "Value of $key: $value")
                        } catch (e: ClassCastException) {
                            ServerTerminal.sendMessage(sender, "Error:" + e.message)
                        } catch (e: IllegalArgumentException) {
                            ServerTerminal.sendMessage(sender, "Error:" + e.message)
                        }
                    } else ServerTerminal.sendMessage(sender, "Usage: settings get {serverKey}")

                    "reload" -> {
                        ServerTerminal.sendMessage(sender, "Reloading.")
                        timeStart = System.nanoTime()
                        try {
                            settingsManager.save()
                            //                            settingsManager.load();
                        } catch (e: ConfigLoadException) {
                            e.printStackTrace()
                        }
                        timeEnd = System.nanoTime()
                        timeElapsed = (timeEnd - timeStart) / 1000000
                        ServerTerminal.sendMessage(sender, "Finished reloading. Took " + timeElapsed + "ms")
                    }

                    "list" -> {
                        ServerTerminal.sendMessage(
                            sender, "Possible setting names : {possible values, empty if any are allowed}"
                        )
                        try {
                            val nameValueMap = settingsManager.configData.getSettingValues(true)
                            for (settingName in nameValueMap.keys) {
                                val possibleValues = nameValueMap[settingName]!!
                                ServerTerminal.sendMessage(sender, "$settingName : $possibleValues")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    "save" -> {
                        ServerTerminal.sendMessage(sender, "Saving.")
                        timeStart = System.nanoTime()
                        try {
                            settingsManager.save()
                        } catch (e: ConfigLoadException) {
                            e.printStackTrace()
                        }
                        timeEnd = System.nanoTime()
                        timeElapsed = (timeEnd - timeStart) / 1000000
                        ServerTerminal.sendMessage(sender, "Finished saving. Took " + timeElapsed + "ms")
                    }

                    else -> ServerTerminal.sendMessage(sender, "No such argument found $arg found")
                }
            }

        }
    }
}