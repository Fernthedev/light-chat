package com.github.fernthedev.lightchat.server.terminal.command

import com.github.fernthedev.config.common.exceptions.ConfigLoadException
import com.github.fernthedev.lightchat.core.ColorCode
import com.github.fernthedev.lightchat.core.data.HashedPassword
import com.github.fernthedev.lightchat.server.SenderInterface
import com.github.fernthedev.lightchat.server.Server
import com.github.fernthedev.lightchat.server.event.AuthenticationAttemptedEvent
import com.github.fernthedev.lightchat.server.event.AuthenticationAttemptedEvent.EventStatus
import com.github.fernthedev.lightchat.server.security.AuthenticationManager.PlayerInfo
import com.github.fernthedev.lightchat.server.terminal.ServerTerminal
import com.github.fernthedev.lightchat.server.terminal.events.ChatEvent
import org.apache.commons.lang3.StringUtils

class AuthTerminalHandler(name: String, private val server: Server) : Command(name) {
    override suspend fun onCommand(sender: SenderInterface, args: Array<String>) {
        if (args.isEmpty()) {
            ServerTerminal.sendMessage(sender, "Please provide new password")
            return
        }

        if (StringUtils.isAlphanumeric(args[0])) {
            val authenticated = server.authenticationManager.authenticate(sender).await()
            ServerTerminal.sendMessage(sender, "Setting password now")
            server.settingsManager.configData.password = args[0]
            try {
                server.settingsManager.save()
            } catch (e: ConfigLoadException) {
                e.printStackTrace()
            }
        } else ServerTerminal.sendMessage(sender, "Password can only be alphanumeric")
    }


    suspend fun onChatEvent(event: ChatEvent) {
        val authenticationManager = server.authenticationManager
        val checking: Map<SenderInterface, PlayerInfo> = authenticationManager.awaitingAuthentications

        if (checking.containsKey(event.sender)) {
            event.isCancelled = true
            server.authenticationManager.attemptAuthenticationHash(HashedPassword(event.message), event.sender)
        }
    }

    suspend fun onAuthenticateEvent(e: AuthenticationAttemptedEvent) {
        when (e.eventStatus) {
            EventStatus.SUCCESS ->                 // Success
                ServerTerminal.sendMessage(
                    e.playerInfo.sender,
                    ColorCode.GREEN.toString() + "Correct password. Successfully authenticated:"
                )

            EventStatus.ATTEMPT_FAILED -> ServerTerminal.sendMessage(
                e.playerInfo.sender,
                ColorCode.RED.toString() + "Incorrect password"
            )

            EventStatus.NO_MORE_TRIES -> ServerTerminal.sendMessage(
                e.playerInfo.sender,
                ColorCode.RED.toString() + "Too many tries. Failed to authenticate"
            )
        }
    }
}