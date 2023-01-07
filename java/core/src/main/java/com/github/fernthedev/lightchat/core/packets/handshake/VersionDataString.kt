package com.github.fernthedev.lightchat.core.packets.handshake

import java.io.Serializable

class VersionDataString(val version: String, val minVersion: String) : Serializable