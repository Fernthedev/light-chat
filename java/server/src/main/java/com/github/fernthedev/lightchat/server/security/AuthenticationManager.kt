package com.github.fernthedev.lightchat.server.security

import com.github.fernthedev.lightchat.core.data.HashedPassword
import com.github.fernthedev.lightchat.core.encryption.transport
import com.github.fernthedev.lightchat.core.encryption.util.EncryptionUtil
import com.github.fernthedev.lightchat.core.packets.SelfMessagePacket
import com.github.fernthedev.lightchat.server.*
import com.github.fernthedev.lightchat.server.event.AuthenticateRequestEvent
import com.github.fernthedev.lightchat.server.event.AuthenticationAttemptedEvent
import com.github.fernthedev.lightchat.server.event.AuthenticationAttemptedEvent.EventStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import org.jetbrains.annotations.Contract
import java.util.concurrent.CompletableFuture

/**
 * Can be used to authenticate command senders.
 */
class AuthenticationManager(private val server: Server) {
    private val checking: MutableMap<SenderInterface, PlayerInfo> = HashMap()


    var amountOfTries = 2
    private set

    /**
     * Runs asynchronously to check if the
     * sender is authenticated
     *
     * Child classes may override this to their liking
     *
     * @param sender authenticator
     * @return future
     *
     * @throws UserIsAuthenticatingException thrown if the user is already attempting authentication
     */
    fun authenticate(sender: SenderInterface): CompletableFuture<Boolean> {
        if (checking.containsKey(sender)) throw UserIsAuthenticatingException("The sender $sender is already attempting authentication.")
        if (sender is Console) {
            return CompletableFuture.completedFuture(true)
        }
        val completableFuture = CompletableFuture<Boolean>()
        val playerInfo = PlayerInfo(sender, completableFuture)
        if (sender is ClientConnection) {
            Dispatchers.Default.asExecutor()
                .execute {
                    playerInfo.mode = Mode.AUTHENTICATE
                    val event = AuthenticateRequestEvent(playerInfo, true)
                    server.eventHandler.callEvent(event)
                    if (event.isCancelled) {
                        completableFuture.complete(playerInfo.authenticated)
                        return@execute
                    }
                    if (!checking.containsKey(sender)) {
                        checking[sender] = playerInfo
                    }
                    sender.sendObject(SelfMessagePacket(SelfMessagePacket.MessageType.FILL_PASSWORD).transport(false))
                }
        }
        return completableFuture
    }

    /**
     * Gets the future of the authentication
     * process for the sender if they are being authenticated
     *
     * @return null if not being authenticated
     */
    @Contract("null -> null")
    fun getUserAuthenticationFuture(sender: SenderInterface): CompletableFuture<Boolean> {
        return checking[sender]!!.future
    }

    /**
     * This requires the password to be hashed, whether the packet is encrypted
     * or not. SelfMessagePacket with FillMessagePacket is sent, it should
     * receive a MessagePacket in return or HashedPasswordPacket with the password hashed.
     * If password given is incorrect, SelfMessagePacket.MessageType.INCORRECT_PASSWORD_ATTEMPT
     * is sent. If authentication finishes with incorrect password,
     * SelfMessagePacket.MessageType.INCORRECT_PASSWORD_FAILURE is sent.
     *
     * @param hashedPassword
     * @param sender
     */
    fun attemptAuthenticationHash(hashedPassword: HashedPassword, sender: SenderInterface) {
        if (checking.containsKey(sender)) {
            val playerInfo = checking[sender]!!
            val event = AuthenticationAttemptedEvent(playerInfo, true,  EventStatus.ATTEMPT_FAILED)
            if (playerInfo.mode == Mode.AUTHENTICATE) {
                val rightPass = EncryptionUtil.makeSHA256Hash(server.settingsManager.configData.password)
                val eventStatus: EventStatus = if (rightPass == hashedPassword.password) {
                    EventStatus.SUCCESS
                } else if (playerInfo.tries <= amountOfTries) {
                    EventStatus.ATTEMPT_FAILED
                } else {
                    EventStatus.NO_MORE_TRIES
                }

                //Handle event
                event.eventStatus = eventStatus
                server.eventHandler.callEvent(event)
                if (event.isCancelled) return
                playerInfo.authenticated = event.eventStatus == EventStatus.SUCCESS
                when (event.eventStatus) {
                    EventStatus.SUCCESS -> {
                        completeAuthentication(playerInfo, true)
                        sender.sendPacket(
                            SelfMessagePacket(SelfMessagePacket.MessageType.CORRECT_PASSWORD).transport(
                                true
                            )
                        )
                    }

                    EventStatus.ATTEMPT_FAILED -> {
                        sender.sendPacket(
                            SelfMessagePacket(SelfMessagePacket.MessageType.INCORRECT_PASSWORD_ATTEMPT).transport(
                                true
                            )
                        )
                        playerInfo.tries++
                    }

                    EventStatus.NO_MORE_TRIES -> {
                        val info: String? = if (sender is ClientConnection) sender.address else ""
                        server.logger.warn("{}:{} tried to authenticate but failed 2 times", sender.name, info)
                        sender.sendPacket(
                            SelfMessagePacket(SelfMessagePacket.MessageType.INCORRECT_PASSWORD_FAILURE).transport(
                                true
                            )
                        )
                        completeAuthentication(playerInfo, false)
                    }
                }
            }
        }
    }

    protected fun completeAuthentication(playerInfo: PlayerInfo?, authenticated: Boolean) {
        playerInfo!!.future.complete(authenticated)
        checking.remove(playerInfo.sender)
    }

    val awaitingAuthentications: Map<SenderInterface, PlayerInfo>
        /**
         * Returns a copy of the authentication queue
         * @return copy of authentication queue
         */
        get() = HashMap(checking)

    enum class Mode {
        AUTHENTICATE
    }

    class PlayerInfo(@JvmField val sender: SenderInterface, var future: CompletableFuture<Boolean>) {
        var authenticated = false
        var mode = Mode.AUTHENTICATE
        var tries = 0
    }
}