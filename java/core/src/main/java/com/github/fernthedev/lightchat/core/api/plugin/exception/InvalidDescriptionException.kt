package com.github.fernthedev.lightchat.core.api.plugin.exception

/**
 * Thrown when attempting to load an invalid PluginDescriptionFile
 */
class InvalidDescriptionException : Exception {
    /**
     * Constructs a new InvalidDescriptionException based on the given
     * Exception
     *
     * @param message Brief message explaining the cause of the exception
     * @param cause Exception that triggered this Exception
     */
    constructor(cause: Throwable?, message: String?) : super(message, cause)

    /**
     * Constructs a new InvalidDescriptionException based on the given
     * Exception
     *
     * @param cause Exception that triggered this Exception
     */
    constructor(cause: Throwable?) : super("Invalid com.github.fernthedev.client.plugin.yml", cause)

    /**
     * Constructs a new InvalidDescriptionException with the given message
     *
     * @param message Brief message explaining the cause of the exception
     */
    constructor(message: String?) : super(message)

    /**
     * Constructs a new InvalidDescriptionException
     */
    constructor() : super("Invalid com.github.fernthedev.client.plugin.yml")

    companion object {
        private const val serialVersionUID = 5721389122281775896L
    }
}