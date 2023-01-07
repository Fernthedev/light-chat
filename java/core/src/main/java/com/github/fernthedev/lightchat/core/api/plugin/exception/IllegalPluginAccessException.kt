package com.github.fernthedev.lightchat.core.api.plugin.exception;

/**
 * Thrown when a com.github.fernthedev.client.plugin attempts to interact with the server when it is not
 * enabled
 */
@SuppressWarnings("serial")
public class IllegalPluginAccessException extends RuntimeException {

    /**
     * Creates a new instance of <code>IllegalPluginAccessException</code>
     * without detail message.
     */
    public IllegalPluginAccessException() {}

    /**
     * Constructs an instance of <code>IllegalPluginAccessException</code>
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public IllegalPluginAccessException(String msg) {
        super(msg);
    }
}
