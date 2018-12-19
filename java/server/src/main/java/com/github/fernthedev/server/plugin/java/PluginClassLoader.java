package com.github.fernthedev.server.plugin.java;

import com.github.fernthedev.server.Server;
import com.github.fernthedev.server.plugin.PluginDescriptionFile;
import com.github.fernthedev.server.plugin.exception.InvalidPluginException;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
final class PluginClassLoader extends URLClassLoader {
    private final JavaPluginLoader loader;
    private final Map<String, Class<?>> classes = new java.util.concurrent.ConcurrentHashMap<String, Class<?>>(); // Spigot
    private final PluginDescriptionFile description;
    private final File dataFolder;
    private final File file;
    private IllegalStateException pluginState;

    // Spigot Start
    static
    {
        try
        {
            java.lang.reflect.Method method = ClassLoader.class.getDeclaredMethod( "registerAsParallelCapable" );
            if ( method != null )
            {
                boolean oldAccessible = method.isAccessible();
                method.setAccessible( true );
                method.invoke( null );
                method.setAccessible( oldAccessible );
                Server.getLogger().info( "Set PluginClassLoader as parallel capable" );
            }
        } catch ( NoSuchMethodException ex )
        {
            // Ignore
        } catch ( Exception ex )
        {
            Server.getLogger().info( "Error setting PluginClassLoader as parallel capable", ex );
        }
    }
    // Spigot End



    PluginClassLoader(final JavaPluginLoader loader, final ClassLoader parent, final PluginDescriptionFile description, final File dataFolder, final File file) throws InvalidPluginException, MalformedURLException {
        super(new URL[] {file.toURI().toURL()}, parent);
        Validate.notNull(loader, "Loader cannot be null");

        this.loader = loader;
        this.description = description;
        this.dataFolder = dataFolder;
        this.file = file;
    }

}
