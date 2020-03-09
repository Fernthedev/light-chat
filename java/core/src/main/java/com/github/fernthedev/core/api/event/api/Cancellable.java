package com.github.fernthedev.core.api.event.api;

public interface Cancellable {

    /**
     * Gets the cancellation state of this com.github.fernthedev.client.event. A cancelled com.github.fernthedev.client.event will not
     * be executed in the server, but will still pass to other plugins
     *
     * @return true if this com.github.fernthedev.client.event is cancelled
     */
    public boolean isCancelled();

    /**
     * Sets the cancellation state of this com.github.fernthedev.client.event. A cancelled com.github.fernthedev.client.event will not
     * be executed in the server, but will still pass to other plugins.
     *
     * @param cancel true if you wish to cancel this com.github.fernthedev.client.event
     */
    public void setCancelled(boolean cancel);
}
