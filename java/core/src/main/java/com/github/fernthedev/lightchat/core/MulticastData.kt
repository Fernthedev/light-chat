package com.github.fernthedev.lightchat.core

class MulticastData {
    constructor()
    constructor(port: Int, verison: String?, minVersion: String?) {
        this.port = port
        version = verison
        this.minVersion = minVersion
        address = address
    }

    constructor(port: Int, version: String?, minVersion: String?, clientNumbers: Int) : this(
        port,
        version,
        minVersion
    ) {
        this.clientNumbers = clientNumbers
    }

    var address: String? = null
    var version: String? = null
    var minVersion: String? = null
    var port = 0
    var clientNumbers = 0
    var clients: List<String> = ArrayList()
}