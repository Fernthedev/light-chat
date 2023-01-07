package com.github.fernthedev.lightchat.core.data

import java.io.Serializable
import java.util.*

class LightCandidate @JvmOverloads constructor(
    value: String?,
    displ: String? = value,
    group: String? = null,
    descr: String? = null,
    suffix: String? = null,
    key: String? = null,
    complete: Boolean = true
) : Serializable {
    @kotlin.jvm.JvmField
    val value: String?
    @kotlin.jvm.JvmField
    val displ: String?
    @kotlin.jvm.JvmField
    val group: String?
    @kotlin.jvm.JvmField
    val descr: String?
    @kotlin.jvm.JvmField
    val suffix: String?
    @kotlin.jvm.JvmField
    val key: String?
    val isComplete: Boolean
    /**
     * Constructs a new Candidate.
     *
     * @param value the value
     * @param displ the display string
     * @param group the group
     * @param descr the description
     * @param suffix the suffix
     * @param key the key
     * @param complete the complete flag
     */
    /**
     * Simple constructor with only a single String as an argument.
     *
     * @param value the candidate
     */
    init {
        Objects.requireNonNull(value)
        this.value = value
        this.displ = displ
        this.group = group
        this.descr = descr
        this.suffix = suffix
        this.key = key
        isComplete = complete
    }
}