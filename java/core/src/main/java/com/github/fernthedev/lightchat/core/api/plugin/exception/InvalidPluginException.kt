package com.github.fernthedev.lightchat.core.api.plugin.exception

/**
 * Thrown when attempting to load an invalid Plugin file
 */
class InvalidPluginException : Exception {
    /**
     * Constructs a new InvalidPluginException based on the given Exception
     *
     * @param cause Exception that triggered this Exception
     */
    constructor(cause: Throwable?) : super(cause)

    /**
     * Constructs a new InvalidPluginException
     */
    constructor()

    /**
     * Constructs a new InvalidPluginException with the specified detail
     * message and cause.
     *
     * @param message the detail message (which is saved for later retrieval
     * by the getMessage() method).
     * @param cause the cause (which is saved for later retrieval by the
     * getCause() method). (A null value is permitted, and indicates that
     * the cause is nonexistent or unknown.)
     */
    constructor(message: String?, cause: Throwable?) : super(message, cause)

    /**
     * Constructs a new InvalidPluginException with the specified detail
     * message
     *
     * @param message TThe detail message is saved for later retrieval by the
     * getMessage() method.
     */
    constructor(message: String?) : super(message)

    companion object {
        private const val serialVersionUID = -8242141640709409544L
    }
}