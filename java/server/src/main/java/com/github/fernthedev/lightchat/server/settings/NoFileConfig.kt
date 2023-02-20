package com.github.fernthedev.lightchat.server.settings

import com.github.fernthedev.config.common.Config
import java.io.File

class NoFileConfig<T : Any> constructor(configData: T) : Config<T>(configData, File(".")) {
    /**
     * Saves the file without verifying the contents of the file
     */
    override fun quickSave() {}
    override fun load(): T {
        return configData
    }

    override fun configToFileString(): String {
        return ""
    }

    /**
     * Returns the object instance of [.configData] parsed from the file which is saved by [.configToFileString]
     *
     * @param data The String data from the file.
     * @return The object instance.
     */
    override fun parseConfigFromData(data: List<String>): T {
        return configData
    }
}