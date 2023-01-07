package com.github.fernthedev.lightchat.core.api.plugin.java

import com.github.fernthedev.lightchat.core.StaticHandler
import com.github.fernthedev.lightchat.core.api.plugin.PluginDescriptionFile
import org.apache.commons.lang3.Validate
import java.io.File
import java.net.URLClassLoader

internal class PluginClassLoader(
    loader: JavaPluginLoader,
    parent: ClassLoader?,
    description: PluginDescriptionFile?,
    dataFolder: File?,
    file: File
) : URLClassLoader(
    arrayOf(file.toURI().toURL()), parent
) {
    // Spigot End
    init {
        Validate.notNull(loader, "Loader cannot be null")
    }

    companion object {
        // Spigot Start
        init {
            try {
                val method = ClassLoader::class.java.getDeclaredMethod("registerAsParallelCapable")
                val oldAccessible = method.isAccessible
                method.isAccessible = true
                method.invoke(null)
                method.isAccessible = oldAccessible
                StaticHandler.core.logger.info("Set PluginClassLoader as parallel capable")
            } catch (ex: NoSuchMethodException) {
                // Ignore
            } catch (ex: Exception) {
                StaticHandler.core.logger.info("Error setting PluginClassLoader as parallel capable", ex)
            }
        }
    }
}