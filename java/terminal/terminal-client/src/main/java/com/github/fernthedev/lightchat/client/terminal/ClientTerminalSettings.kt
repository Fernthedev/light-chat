package com.github.fernthedev.lightchat.client.terminal

import com.github.fernthedev.config.common.Config
import com.github.fernthedev.config.gson.GsonConfig
import com.github.fernthedev.lightchat.client.ClientSettings
import java.io.File

class ClientTerminalSettings internal constructor(
    host: String?,
    port: Int,
    clientSettings: Config<out ClientSettings>,
    allowTermPackets: Boolean,
    launchConsoleInCMDWhenNone: Boolean,
    consoleCommandHandler: Boolean,
    checkForServersInMulticast: Boolean,
    askUserForHostPort: Boolean,
    shutdownOnDisconnect: Boolean
) {
    var host: String? = null
        protected set
    var port = -1
        protected set
    var clientSettings = createConfigWithoutException()
        protected set
    var isAllowTermPackets = true
        protected set
    var isLaunchConsoleInCMDWhenNone = true
        protected set
    var isConsoleCommandHandler = true
        protected set
    var isCheckForServersInMulticast = true
        protected set
    var isAskUserForHostPort = true
        protected set
    var isShutdownOnDisconnect = true
        protected set

    init {
        this.host = host
        this.port = port
        this.clientSettings = clientSettings
        isAllowTermPackets = allowTermPackets
        isLaunchConsoleInCMDWhenNone = launchConsoleInCMDWhenNone
        isConsoleCommandHandler = consoleCommandHandler
        isCheckForServersInMulticast = checkForServersInMulticast
        isAskUserForHostPort = askUserForHostPort
        isShutdownOnDisconnect = shutdownOnDisconnect
    }

    class ClientTerminalSettingsBuilder internal constructor() {
        private var `host$value`: String? = null
        private var `host$set` = false
        private var `port$value` = 0
        private var `port$set` = false
        private var `clientSettings$value`: Config<out ClientSettings>? = null
        private var `clientSettings$set` = false
        private var `allowTermPackets$value` = false
        private var `allowTermPackets$set` = false
        private var `launchConsoleInCMDWhenNone$value` = false
        private var `launchConsoleInCMDWhenNone$set` = false
        private var `consoleCommandHandler$value` = false
        private var `consoleCommandHandler$set` = false
        private var `checkForServersInMulticast$value` = false
        private var `checkForServersInMulticast$set` = false
        private var `askUserForHostPort$value` = false
        private var `askUserForHostPort$set` = false
        private var `shutdownOnDisconnect$value` = false
        private var `shutdownOnDisconnect$set` = false
        fun host(host: String?): ClientTerminalSettingsBuilder {
            `host$value` = host
            `host$set` = true
            return this
        }

        fun port(port: Int): ClientTerminalSettingsBuilder {
            `port$value` = port
            `port$set` = true
            return this
        }

        fun clientSettings(clientSettings: Config<out ClientSettings>): ClientTerminalSettingsBuilder {
            `clientSettings$value` = clientSettings
            `clientSettings$set` = true
            return this
        }

        fun allowTermPackets(allowTermPackets: Boolean): ClientTerminalSettingsBuilder {
            `allowTermPackets$value` = allowTermPackets
            `allowTermPackets$set` = true
            return this
        }

        fun launchConsoleInCMDWhenNone(launchConsoleInCMDWhenNone: Boolean): ClientTerminalSettingsBuilder {
            `launchConsoleInCMDWhenNone$value` = launchConsoleInCMDWhenNone
            `launchConsoleInCMDWhenNone$set` = true
            return this
        }

        fun consoleCommandHandler(consoleCommandHandler: Boolean): ClientTerminalSettingsBuilder {
            `consoleCommandHandler$value` = consoleCommandHandler
            `consoleCommandHandler$set` = true
            return this
        }

        fun checkForServersInMulticast(checkForServersInMulticast: Boolean): ClientTerminalSettingsBuilder {
            `checkForServersInMulticast$value` = checkForServersInMulticast
            `checkForServersInMulticast$set` = true
            return this
        }

        fun askUserForHostPort(askUserForHostPort: Boolean): ClientTerminalSettingsBuilder {
            `askUserForHostPort$value` = askUserForHostPort
            `askUserForHostPort$set` = true
            return this
        }

        fun shutdownOnDisconnect(shutdownOnDisconnect: Boolean): ClientTerminalSettingsBuilder {
            `shutdownOnDisconnect$value` = shutdownOnDisconnect
            `shutdownOnDisconnect$set` = true
            return this
        }

        fun build(): ClientTerminalSettings {
            var `host$value` = `host$value`
            if (!`host$set`) {
                `host$value` = `$default$host`()
            }
            var `port$value` = `port$value`
            if (!`port$set`) {
                `port$value` = `$default$port`()
            }
            var `clientSettings$value` = `clientSettings$value`
            if (!`clientSettings$set`) {
                `clientSettings$value` = `$default$clientSettings`()
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
            var `checkForServersInMulticast$value` = `checkForServersInMulticast$value`
            if (!`checkForServersInMulticast$set`) {
                `checkForServersInMulticast$value` = `$default$checkForServersInMulticast`()
            }
            var `askUserForHostPort$value` = `askUserForHostPort$value`
            if (!`askUserForHostPort$set`) {
                `askUserForHostPort$value` = `$default$askUserForHostPort`()
            }
            var `shutdownOnDisconnect$value` = `shutdownOnDisconnect$value`
            if (!`shutdownOnDisconnect$set`) {
                `shutdownOnDisconnect$value` = `$default$shutdownOnDisconnect`()
            }
            return ClientTerminalSettings(
                `host$value`,
                `port$value`,
                `clientSettings$value`!!,
                `allowTermPackets$value`,
                `launchConsoleInCMDWhenNone$value`,
                `consoleCommandHandler$value`,
                `checkForServersInMulticast$value`,
                `askUserForHostPort$value`,
                `shutdownOnDisconnect$value`
            )
        }

        override fun toString(): String {
            return "ClientTerminalSettings.ClientTerminalSettingsBuilder(host\$value=$`host$value`, port\$value=$`port$value`, clientSettings\$value=$`clientSettings$value`, allowTermPackets\$value=$`allowTermPackets$value`, launchConsoleInCMDWhenNone\$value=$`launchConsoleInCMDWhenNone$value`, consoleCommandHandler\$value=$`consoleCommandHandler$value`, checkForServersInMulticast\$value=$`checkForServersInMulticast$value`, askUserForHostPort\$value=$`askUserForHostPort$value`, shutdownOnDisconnect\$value=$`shutdownOnDisconnect$value`)"
        }
    }

    companion object {
        protected fun createConfigWithoutException(): Config<out ClientSettings> {
            return GsonConfig(ClientSettings(), File("client_settings.json"))
        }

        private fun `$default$host`(): String? {
            return null
        }

        private fun `$default$port`(): Int {
            return -1
        }

        private fun `$default$clientSettings`(): Config<out ClientSettings> {
            return createConfigWithoutException()
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

        private fun `$default$checkForServersInMulticast`(): Boolean {
            return true
        }

        private fun `$default$askUserForHostPort`(): Boolean {
            return true
        }

        private fun `$default$shutdownOnDisconnect`(): Boolean {
            return true
        }

        fun builder(): ClientTerminalSettingsBuilder {
            return ClientTerminalSettingsBuilder()
        }
    }
}