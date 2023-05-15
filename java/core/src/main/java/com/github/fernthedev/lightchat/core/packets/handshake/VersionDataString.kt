package com.github.fernthedev.lightchat.core.packets.handshake

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
class VersionDataString(val version: String, val minVersion: String) : Serializable