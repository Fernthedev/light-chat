package com.github.fernthedev.lightchat.core.api.plugin.exception

/**
 * Thrown when a com.github.fernthedev.client.plugin attempts to interact with the server when it is not
 * enabled
 */
class IllegalPluginAccessException : RuntimeException {
    /**
     * Creates a new instance of `IllegalPluginAccessException`
     * without detail message.
     */
    constructor()

    /**
     * Constructs an instance of `IllegalPluginAccessException`
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    constructor(msg: String?) : super(msg)
}