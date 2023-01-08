package com.github.fernthedev.lightchat.server.terminal

import com.github.fernthedev.config.common.Config
import com.github.fernthedev.config.gson.GsonConfig
import com.github.fernthedev.lightchat.server.settings.ServerSettings
import lombok.SneakyThrows
import java.io.File

//@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ServerTerminalSettings internal constructor(
    serverSettings: Config<out ServerSettings>,
    allowChangePassword: Boolean,
    allowTermPackets: Boolean,
    launchConsoleInCMDWhenNone: Boolean,
    consoleCommandHandler: Boolean,
    port: Int
) {
    var serverSettings = createConfigWithoutException()
    var isAllowChangePassword = true
        protected set
    var isAllowTermPackets = true
        protected set
    var isLaunchConsoleInCMDWhenNone = true
        protected set
    var isConsoleCommandHandler = true
        protected set
    var port = -1

    init {
        this.serverSettings = serverSettings
        isAllowChangePassword = allowChangePassword
        isAllowTermPackets = allowTermPackets
        isLaunchConsoleInCMDWhenNone = launchConsoleInCMDWhenNone
        isConsoleCommandHandler = consoleCommandHandler
        this.port = port
    }

    class ServerTerminalSettingsBuilder internal constructor() {
        private var `serverSettings$value`: Config<out ServerSettings>? = null
        private var `serverSettings$set` = false
        private var `allowChangePassword$value` = false
        private var `allowChangePassword$set` = false
        private var `allowTermPackets$value` = false
        private var `allowTermPackets$set` = false
        private var `launchConsoleInCMDWhenNone$value` = false
        private var `launchConsoleInCMDWhenNone$set` = false
        private var `consoleCommandHandler$value` = false
        private var `consoleCommandHandler$set` = false
        private var `port$value` = 0
        private var `port$set` = false
        fun serverSettings(serverSettings: Config<out ServerSettings>): ServerTerminalSettingsBuilder {
            `serverSettings$value` = serverSettings
            `serverSettings$set` = true
            return this
        }

        fun allowChangePassword(allowChangePassword: Boolean): ServerTerminalSettingsBuilder {
            `allowChangePassword$value` = allowChangePassword
            `allowChangePassword$set` = true
            return this
        }

        fun allowTermPackets(allowTermPackets: Boolean): ServerTerminalSettingsBuilder {
            `allowTermPackets$value` = allowTermPackets
            `allowTermPackets$set` = true
            return this
        }

        fun launchConsoleInCMDWhenNone(launchConsoleInCMDWhenNone: Boolean): ServerTerminalSettingsBuilder {
            `launchConsoleInCMDWhenNone$value` = launchConsoleInCMDWhenNone
            `launchConsoleInCMDWhenNone$set` = true
            return this
        }

        fun consoleCommandHandler(consoleCommandHandler: Boolean): ServerTerminalSettingsBuilder {
            `consoleCommandHandler$value` = consoleCommandHandler
            `consoleCommandHandler$set` = true
            return this
        }

        fun port(port: Int): ServerTerminalSettingsBuilder {
            `port$value` = port
            `port$set` = true
            return this
        }

        fun build(): ServerTerminalSettings {
            var `serverSettings$value` = `serverSettings$value`
            if (!`serverSettings$set`) {
                `serverSettings$value` = `$default$serverSettings`()
            }
            var `allowChangePassword$value` = `allowChangePassword$value`
            if (!`allowChangePassword$set`) {
                `allowChangePassword$value` = `$default$allowChangePassword`()
            }
            var `allowTermPackets$value` = `allowTermPackets$value`
            if (!`allowTermPackets$set`) {
                `allowTermPackets$value` = `$default$allowTermPackets`()
            }
            var `launchConsoleInCMDWhenNone$value` = `launchConsoleInCMDWhenNone$value`
            if (!`launchConsoleInCMDWhenNone$set`) {
                `launchConsoleInCMDWhenNone$value` = `$default$launchConsoleInCMDWhenNone`()
            }
            var `consoleCommandHandler$value` = `consoleCommandHandler$value`
            if (!`consoleCommandHandler$set`) {
                `consoleCommandHandler$value` = `$default$consoleCommandHandler`()
            }
            var `port$value` = `port$value`
            if (!`port$set`) {
                `port$value` = `$default$port`()
            }
            return ServerTerminalSettings(
                `serverSettings$value`!!,
                `allowChangePassword$value`,
                `allowTermPackets$value`,
                `launchConsoleInCMDWhenNone$value`,
                `consoleCommandHandler$value`,
                `port$value`
            )
        }

        override fun toString(): String {
            return "ServerTerminalSettings.ServerTerminalSettingsBuilder(serverSettings\$value=$`serverSettings$value`, allowChangePassword\$value=$`allowChangePassword$value`, allowTermPackets\$value=$`allowTermPackets$value`, launchConsoleInCMDWhenNone\$value=$`launchConsoleInCMDWhenNone$value`, consoleCommandHandler\$value=$`consoleCommandHandler$value`, port\$value=$`port$value`)"
        }
    }

    companion object {
        @SneakyThrows
        protected fun createConfigWithoutException(): Config<out ServerSettings> {
            return GsonConfig(ServerSettings(), File("settings.json"))
        }

        private fun `$default$serverSettings`(): Config<out ServerSettings> {
            return createConfigWithoutException()
        }

        private fun `$default$allowChangePassword`(): Boolean {
            return true
        }

        private fun `$default$allowTermPackets`(): Boolean {
            return true
        }

        private fun `$default$launchConsoleInCMDWhenNone`(): Boolean {
            return true
        }

        private fun `$default$consoleCommandHandler`(): Boolean {
            return true
        }

        private fun `$default$port`(): Int {
            return -1
        }

        fun builder(): ServerTerminalSettingsBuilder {
            return ServerTerminalSettingsBuilder()
        }
    }
}