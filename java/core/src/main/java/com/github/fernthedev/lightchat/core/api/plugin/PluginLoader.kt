package com.github.fernthedev.lightchat.core.api.plugin;

import com.github.fernthedev.lightchat.core.api.event.api.Event;
import com.github.fernthedev.lightchat.core.api.event.api.Listener;
import com.github.fernthedev.lightchat.core.api.event.api.RegisteredListener;
import com.github.fernthedev.lightchat.core.api.plugin.exception.InvalidPluginException;
import com.github.fernthedev.lightchat.core.api.plugin.exception.UnknownDependencyException;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Represents a com.github.fernthedev.client.plugin loader, which handles direct access to specific types
 * of plugins
 */
public interface PluginLoader {

    /**
     * Loads the com.github.fernthedev.client.plugin contained in the specified file
     *
     * @param file File to attempt to load
     * @return Plugin that was contained in the specified file, or null if
     *     unsuccessful
     * @throws InvalidPluginException Thrown when the specified file is not a
     *     com.github.fernthedev.client.plugin
     * @throws UnknownDependencyException If a required dependency could not
     *     be found
     */
    public Plugin loadPlugin(File file) throws InvalidPluginException, UnknownDependencyException;

    /**
     * Returns a list of all filename filters expected by this PluginLoader
     *
     * @return The filters
     */
    public Pattern[] getPluginFileFilters();

    /**
     * Creates and returns registered listeners for the com.github.fernthedev.client.event classes used in
     * this listener
     *
     * @param listener The object that will handle the eventual call back
     * @param plugin The com.github.fernthedev.client.plugin to use when creating registered listeners
     * @return The registered listeners.
     */
    public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(Listener listener, Plugin plugin);

    /**
     * Disables the specified com.github.fernthedev.client.plugin
     * <p>
     * Attempting to disable a com.github.fernthedev.client.plugin that is not enabled will have no effect
     *
     * @param plugin Plugin to disable
     */
    public void disablePlugin(Plugin plugin);
}