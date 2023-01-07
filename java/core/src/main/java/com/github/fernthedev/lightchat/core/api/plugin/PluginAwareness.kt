package com.github.fernthedev.lightchat.core.api.plugin

/**
 * Represents a concept that a com.github.fernthedev.client.plugin is aware of.
 *
 *
 * The internal representation may be singleton, or be a parameterized
 * instance, but must be immutable.
 */
interface PluginAwareness {
    /**
     * Each entry here represents a particular com.github.fernthedev.client.plugin's awareness. These can
     * be checked by using [PluginDescriptionFile.getAwareness].[ ][Set.contains].
     */
    enum class Flags : PluginAwareness {
        /**
         * This specifies that all (text) resources stored in a com.github.fernthedev.client.plugin's jar
         * use UTF-8 encoding.
         *
         */
        @Deprecated("all plugins are now assumed to be UTF-8 aware.")
        UTF8
    }
}