package com.github.fernthedev.lightchat.core.exceptions

class ParsePacketException : IllegalArgumentException {
    /**
     * Constructs an `IllegalArgumentException` with no
     * detail message.
     */
    constructor() : super()

    /**
     * Constructs an `IllegalArgumentException` with the
     * specified detail message.
     *
     * @param s the detail message.
     */
    constructor(s: String?) : super(s)

    /**
     * Constructs a new exception with the specified detail message and
     * cause.
     *
     *
     * Note that the detail message associated with `cause` is
     * *not* automatically incorporated in this exception's detail
     * message.
     *
     * @param message the detail message (which is saved for later retrieval
     * by the [Throwable.getMessage] method).
     * @param cause   the cause (which is saved for later retrieval by the
     * [Throwable.getCause] method).  (A `null` value
     * is permitted, and indicates that the cause is nonexistent or
     * unknown.)
     * @since 1.5
     */
    constructor(message: String?, cause: Throwable?) : super(message, cause)

    /**
     * Constructs a new exception with the specified cause and a detail
     * message of `(cause==null ? null : cause.toString())` (which
     * typically contains the class and detail message of `cause`).
     * This constructor is useful for exceptions that are little more than
     * wrappers for other throwables (for example, [ ]).
     *
     * @param cause the cause (which is saved for later retrieval by the
     * [Throwable.getCause] method).  (A `null` value is
     * permitted, and indicates that the cause is nonexistent or
     * unknown.)
     * @since 1.5
     */
    constructor(cause: Throwable?) : super(cause)
}