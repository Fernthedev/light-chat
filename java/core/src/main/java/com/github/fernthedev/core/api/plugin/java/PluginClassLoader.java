package com.github.fernthedev.core.api.plugin.java;

import com.github.fernthedev.core.StaticHandler;
import com.github.fernthedev.core.api.plugin.PluginDescriptionFile;
import com.github.fernthedev.core.api.plugin.exception.InvalidPluginException;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

final class PluginClassLoader extends URLClassLoader {

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
                StaticHandler.getCore().getLogger().info( "Set PluginClassLoader as parallel capable" );
            }
        } catch ( NoSuchMethodException ex )
        {
            // Ignore
        } catch ( Exception ex )
        {
            StaticHandler.getCore().getLogger().info( "Error setting PluginClassLoader as parallel capable", ex );
        }
    }
    // Spigot End



    PluginClassLoader(final JavaPluginLoader loader, final ClassLoader parent, final PluginDescriptionFile description, final File dataFolder, final File file) throws InvalidPluginException, MalformedURLException {
        super(new URL[] {file.toURI().toURL()}, parent);
        Validate.notNull(loader, "Loader cannot be null");

    }

}
