package com.github.fernthedev.lightchat.core

import com.github.fernthedev.lightchat.core.packets.handshake.VersionDataString
import org.apache.maven.artifact.versioning.DefaultArtifactVersion

data class VersionData
@JvmOverloads
constructor(
    val version: DefaultArtifactVersion,
    val minVersion: DefaultArtifactVersion,
    val variablesJSON: VariablesJSON? = null,
) {

    constructor(variablesJSON: VariablesJSON) : this(variablesJSON = variablesJSON, version = DefaultArtifactVersion(variablesJSON.version), minVersion = DefaultArtifactVersion(variablesJSON.minVersion))
    constructor(variablesJSON: VersionDataString) : this(variablesJSON = null, version = DefaultArtifactVersion(variablesJSON.version), minVersion = DefaultArtifactVersion(variablesJSON.minVersion))
    constructor(v: String, mv: String) : this(variablesJSON = null, version = DefaultArtifactVersion(v), minVersion = DefaultArtifactVersion(mv))

}