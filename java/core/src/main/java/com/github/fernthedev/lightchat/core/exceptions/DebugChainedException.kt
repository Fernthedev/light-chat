package com.github.fernthedev.lightchat.core.exceptions

class DebugChainedException : DebugException {
    private var exception: Exception? = null
    override val message: String? = null

    constructor() {
        IllegalArgumentException("Use DebugException instead").printStackTrace()
    }

    constructor(exception: Exception?, message: String?) : super(message) {
        this.exception = exception
    }

    override fun printStackTrace() {
        println(message)
        super.printStackTrace()
        initCause(exception)
        System.err.println("Caused by: $cause")
        cause!!.printStackTrace()
    }
}