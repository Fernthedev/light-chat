package com.github.fernthedev.lightchat.core.exceptions

class ErrorMessageException : Exception {
    constructor(string: String?) : super(string)
    constructor(ex: Exception?) : super(ex)
    constructor(s: String?, ex: Exception?) : super(s, ex)
}