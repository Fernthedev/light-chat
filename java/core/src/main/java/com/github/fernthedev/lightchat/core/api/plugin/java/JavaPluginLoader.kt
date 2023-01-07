package com.github.fernthedev.lightchat.core.api.plugin.java

import com.github.fernthedev.lightchat.core.StaticHandler
import com.github.fernthedev.lightchat.core.api.event.api.*
import com.github.fernthedev.lightchat.core.api.plugin.*
import com.github.fernthedev.lightchat.core.api.plugin.exception.InvalidDescriptionException
import com.github.fernthedev.lightchat.core.api.plugin.exception.InvalidPluginException
import com.github.fernthedev.lightchat.core.api.plugin.exception.UnknownDependencyException
import org.apache.commons.lang3.Validate
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*
import java.util.jar.JarFile
import java.util.regex.Pattern

/**
 * Represents a Java com.github.fernthedev.client.plugin loader, allowing plugins in the form of .jar
 */
class JavaPluginLoader @Deprecated("") constructor(instance: Any) : PluginLoader {
    private val server: Any
    private val fileFilters = arrayOf(Pattern.compile("\\.jar$"))

    /**
     * This class was not meant to be constructed explicitly
     *
     * @param instance the server instance
     */
    init {
        Validate.notNull(instance, "Server cannot be null")
        server = instance
    }

    /*public Plugin loadPlugin(final File file) throws InvalidPluginException {
        Validate.notNull(file, "File cannot be null");

        if (!file.exists()) {
            throw new InvalidPluginException(new FileNotFoundException(file.getPath() + " does not exist"));
        }

        final PluginDescriptionFile description;
        try {
            description = getPluginDescription(file);
        } catch (InvalidDescriptionException ex) {
            throw new InvalidPluginException(ex);
        }

        final File parentFile = file.getParentFile();
        final File dataFolder = new File(parentFile, description.getName());
        @SuppressWarnings("deprecation")
        final File oldDataFolder = new File(parentFile, description.getRawName());

        // Found old data folder
        if (dataFolder.equals(oldDataFolder)) {
            // They are equal -- nothing needs to be done!
        } else if (dataFolder.isDirectory() && oldDataFolder.isDirectory()) {
            server.getLogger().error(String.format(
                    "While loading %s (%s) found old-data folder: `%s' next to the new one `%s'",
                    description.getFullName(),
                    file,
                    oldDataFolder,
                    dataFolder
            ));
        } else if (oldDataFolder.isDirectory() && !dataFolder.exists()) {
            if (!oldDataFolder.renameTo(dataFolder)) {
                throw new InvalidPluginException("Unable to rename old data folder: `" + oldDataFolder + "' to: `" + dataFolder + "'");
            }
            server.getLogger().info(String.format(
                    "While loading %s (%s) renamed data folder: `%s' to `%s'",
                    description.getFullName(),
                    file,
                    oldDataFolder,
                    dataFolder
            ));
        }

        if (dataFolder.exists() && !dataFolder.isDirectory()) {
            throw new InvalidPluginException(String.format(
                    "Projected datafolder: `%s' for %s (%s) exists and is not a directory",
                    dataFolder,
                    description.getFullName(),
                    file
            ));
        }

        for (final String pluginName : description.getDepend()) {
            Plugin current = server.getPluginManager().getPlugin(pluginName);

            if (current == null) {
                throw new UnknownDependencyException(pluginName);
            }
        }

        final PluginClassLoader loader;
        try {
            loader = new PluginClassLoader(this, getClass().getClassLoader(), description, dataFolder, file);
        } catch (InvalidPluginException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidPluginException(ex);
        }

        loaders.add(loader);

        return loader.com.github.fernthedev.client.plugin;
    }*/
    @Throws(InvalidDescriptionException::class)
    fun getPluginDescription(file: File): PluginDescriptionFile {
        Validate.notNull(file, "File cannot be null")
        var jar: JarFile? = null
        var stream: InputStream? = null
        return try {
            jar = JarFile(file)
            val entry = jar.getJarEntry("com.github.fernthedev.client.plugin.yml")
                ?: throw InvalidDescriptionException(FileNotFoundException("Jar does not contain com.github.fernthedev.client.plugin.yml"))
            stream = jar.getInputStream(entry)
            PluginDescriptionFile(stream)
        } catch (ex: IOException) {
            throw InvalidDescriptionException(ex)
        } /*catch (YAMLException ex) {
            throw new InvalidDescriptionException(ex);
        } */ finally {
            if (jar != null) {
                try {
                    jar.close()
                } catch (e: IOException) {
                }
            }
            if (stream != null) {
                try {
                    stream.close()
                } catch (e: IOException) {
                }
            }
        }
    }

    /**
     * Loads the com.github.fernthedev.client.plugin contained in the specified file
     *
     * @param file File to attempt to load
     * @return Plugin that was contained in the specified file, or null if
     * unsuccessful
     * @throws InvalidPluginException     Thrown when the specified file is not a
     * com.github.fernthedev.client.plugin
     * @throws UnknownDependencyException If a required dependency could not
     * be found
     */
    @Throws(InvalidPluginException::class, UnknownDependencyException::class)
    override fun loadPlugin(file: File?): Plugin? {
        return null
    }

    override val pluginFileFilters: Array<Pattern>?
        get() = fileFilters.clone()

    override fun createRegisteredListeners(
        listener: Listener,
        plugin: Plugin
    ): Map<Class<out Event>, MutableSet<RegisteredListener>> {
        Validate.notNull(plugin, "Plugin can not be null")
        Validate.notNull(listener, "Listener can not be null")
        val ret: MutableMap<Class<out Event>, MutableSet<RegisteredListener>> = HashMap()
        val methods: Set<Method>
        try {
            val publicMethods = listener.javaClass.methods
            val privateMethods = listener.javaClass.declaredMethods
            methods = HashSet(publicMethods.size + privateMethods.size, 1.0f)
            Collections.addAll(methods, *publicMethods)
            Collections.addAll(methods, *privateMethods)
        } catch (e: NoClassDefFoundError) {
            StaticHandler.core.logger.error("Plugin " + " has failed to register events for " + listener.javaClass + " because " + e.message + " does not exist.")
            return ret
        }
        for (method in methods) {
            val eh = method.getAnnotation(
                EventHandler::class.java
            ) ?: continue
            // Do not register bridge or synthetic methods to avoid com.github.fernthedev.client.event duplication
            // Fixes SPIGOT-893
            if (method.isBridge || method.isSynthetic) {
                continue
            }
            lateinit var checkClass: Class<*>
            if (method.parameterTypes.size != 1 || !Event::class.java.isAssignableFrom(
                    method.parameterTypes[0].also { checkClass = it })
            ) {
                StaticHandler.core.logger.info(" attempted to register an invalid EventHandler method signature \"" + method.toGenericString() + "\" in " + listener.javaClass)
                continue
            }
            val eventClass = checkClass.asSubclass(
                Event::class.java
            )
            method.isAccessible = true
            var eventSet = ret[eventClass]
            if (eventSet == null) {
                eventSet = HashSet()
                ret[eventClass] = eventSet
            }
            val executor = object : EventExecutor {
                override fun execute(listener: Listener, event: Event) {
                    try {
                        if (!event.javaClass.let { eventClass.isAssignableFrom(it) }) {
                            return
                        }
                        // Spigot start
                        method.invoke(listener, event)
                        // Spigot end
                    } catch (ex: InvocationTargetException) {
                        throw EventException(ex.cause!!)
                    } catch (t: Throwable) {
                        throw EventException(t)
                    }
                }
            }
            eventSet.add(RegisteredListener(listener, executor, plugin, eh.priority, eh.ignoreCancelled))
        }
        return ret
    }

    /**
     * Disables the specified com.github.fernthedev.client.plugin
     *
     *
     * Attempting to disable a com.github.fernthedev.client.plugin that is not enabled will have no effect
     *
     * @param plugin Plugin to disable
     */
    override fun disablePlugin(plugin: Plugin?) {}
}