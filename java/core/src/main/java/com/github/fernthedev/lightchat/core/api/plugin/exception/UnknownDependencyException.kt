package com.github.fernthedev.lightchat.core.api.plugin.exception

/**
 * Thrown when attempting to load an invalid Plugin file
 */
class UnknownDependencyException : RuntimeException {
    /**
     * Constructs a new UnknownDependencyException based on the given
     * Exception
     *
     * @param throwable Exception that triggered this Exception
     */
    constructor(throwable: Throwable?) : super(throwable)

    /**
     * Constructs a new UnknownDependencyException with the given message
     *
     * @param message Brief message explaining the cause of the exception
     */
    constructor(message: String?) : super(message)

    /**
     * Constructs a new UnknownDependencyException based on the given
     * Exception
     *
     * @param message Brief message explaining the cause of the exception
     * @param throwable Exception that triggered this Exception
     */
    constructor(throwable: Throwable?, message: String?) : super(message, throwable)

    /**
     * Constructs a new UnknownDependencyException
     */
    constructor()

    companion object {
        private const val serialVersionUID = 5721389371901775895L
    }
}