package com.github.fernthedev.lightchat.core.api.plugin

/**
 * Represents the order in which a com.github.fernthedev.client.plugin should be initialized and enabled
 */
enum class PluginLoadOrder {
    /**
     * Indicates that the com.github.fernthedev.client.plugin will be loaded at startup
     */
    STARTUP,

    /**
     * Indicates that the com.github.fernthedev.client.plugin will be loaded after the first/default world
     * was created
     */
    POSTWORLD
}