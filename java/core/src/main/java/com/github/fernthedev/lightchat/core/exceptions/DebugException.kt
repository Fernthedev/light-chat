package com.github.fernthedev.lightchat.core.exceptions

open class DebugException : RuntimeException {
    constructor()
    constructor(message: String?) : super(message)

    /**
     * Constructs a new runtime exception with the specified detail message and
     * cause.
     *
     *Note that the detail message associated with
     * `cause` is *not* automatically incorporated in
     * this runtime exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     * by the [.getMessage] method).
     * @param cause   the cause (which is saved for later retrieval by the
     * [.getCause] method).  (A `null` value is
     * permitted, and indicates that the cause is nonexistent or
     * unknown.)
     * @since 1.4
     */
    constructor(message: String?, cause: Throwable?) : super(message, cause)

    /**
     * Constructs a new runtime exception with the specified detail
     * message, cause, suppression enabled or disabled, and writable
     * stack trace enabled or disabled.
     *
     * @param message            the detail message.
     * @param cause              the cause.  (A `null` value is permitted,
     * and indicates that the cause is nonexistent or unknown.)
     * @param enableSuppression  whether or not suppression is enabled
     * or disabled
     * @param writableStackTrace whether or not the stack trace should
     * be writable
     * @since 1.7
     */
    protected constructor(
        message: String?,
        cause: Throwable?,
        enableSuppression: Boolean,
        writableStackTrace: Boolean
    ) : super(message, cause, enableSuppression, writableStackTrace)

    constructor(throwable: Throwable?) : super(throwable)
}