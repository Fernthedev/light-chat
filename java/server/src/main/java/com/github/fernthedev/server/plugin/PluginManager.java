package com.github.fernthedev.server.plugin;

import com.github.fernthedev.server.Server;
import com.github.fernthedev.server.event.*;
import com.github.fernthedev.server.plugin.exception.IllegalPluginAccessException;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PluginManager {

    private List<Listener> listeners = new ArrayList<>();

    /**
     * Calls an event with the given details.
     * <p>
     * This method only synchronizes when the event is not asynchronous.
     *
     * @param event Event details
     */
    public void callEvent(Event event) {
        if (event.isAsynchronous()) {
            if (Thread.holdsLock(this)) {
                throw new IllegalStateException(event.getEventName() + " cannot be triggered asynchronously from inside synchronized code.");
            }
            fireEvent(event);
        } else {
            synchronized (this) {
                fireEvent(event);
            }
        }
    }

    private void fireEvent(Event event) {
        HandlerList handlers = event.getHandlers();
        RegisteredListener[] listeners = handlers.getRegisteredListeners();

        for (RegisteredListener registration : listeners) {

            try {
                registration.callEvent(event);
            } catch (Exception ex) {
                Server.getLogger().error("Could not pass event " + event.getEventName() + " to ", ex);
            }
        }
    }

    /**
     * Registers the given event to the specified listener using a directly
     * passed EventExecutor
     *
     * @param event Event class to register
     * @param listener PlayerListener to register
     * @param priority Priority of this event
     * @param executor EventExecutor to register
     * @param plugin Plugin to register
     * @param ignoreCancelled Do not call executor if event was already
     *     cancelled
     */
    public void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority, EventExecutor executor, Plugin plugin, boolean ignoreCancelled) {
        Validate.notNull(listener, "Listener cannot be null");
        Validate.notNull(priority, "Priority cannot be null");
        Validate.notNull(executor, "Executor cannot be null");
        Validate.notNull(plugin, "Plugin cannot be null");

        getEventListeners(event).register(new RegisteredListener(listener, executor,plugin, priority, ignoreCancelled));

    }

    public void registerEvents(Listener listener, Plugin plugin) {
        for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry : plugin.createRegisteredListeners(listener, plugin).entrySet()) {
            getEventListeners(getRegistrationClass(entry.getKey())).registerAll(entry.getValue());
        }
    }

    public void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority, EventExecutor executor, Plugin plugin) {
        registerEvent(event, listener, priority, executor, plugin, false);
    }

    private HandlerList getEventListeners(Class<? extends Event> type) {
        try {
            Method method = getRegistrationClass(type).getDeclaredMethod("getHandlerList");
            method.setAccessible(true);
            return (HandlerList) method.invoke(null);
        } catch (Exception e) {
            throw new IllegalPluginAccessException(e.toString());
        }
    }


    private Class<? extends Event> getRegistrationClass(Class<? extends Event> clazz) {
        try {
            clazz.getDeclaredMethod("getHandlerList");
            return clazz;
        } catch (NoSuchMethodException e) {
            if (clazz.getSuperclass() != null
                    && !clazz.getSuperclass().equals(Event.class)
                    && Event.class.isAssignableFrom(clazz.getSuperclass())) {
                return getRegistrationClass(clazz.getSuperclass().asSubclass(Event.class));
            } else {
                throw new IllegalPluginAccessException("Unable to find handler list for event " + clazz.getName() + ". Static getHandlerList method required!");
            }
        }
    }

    public void registerListener(Listener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(Listener listener) {
        listeners.remove(listener);
    }}
