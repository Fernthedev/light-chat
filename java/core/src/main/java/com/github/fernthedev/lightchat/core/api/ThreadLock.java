package com.github.fernthedev.lightchat.core.api;

import lombok.Getter;

public class ThreadLock {

    private final Object threadLockObject = new Object();


    @Getter
    private boolean lock = false;

    public void lock() {
        lock = true;
    }

    /**
     * Notifies all threads that the object is ready
     *
     * See {@link Object#notifyAll()}
     *
     */
    public void notifyAllThreads() {
        lock = false;
        synchronized (threadLockObject) {
            threadLockObject.notifyAll();
        }
    }

    /**
     * Waits on the {@link #threadLockObject}
     *
     * See {@link Object#wait()}
     *
     * @throws InterruptedException
     */
    public void waitOnLock() throws InterruptedException {
        threadLockObject.wait();
    }

    /**
     * Waits on the {@link #threadLockObject}
     *
     * See {@link Object#wait(long)}
     *
     * @throws InterruptedException
     */
    public void waitOnLock(long timeoutMillis) throws InterruptedException {
        threadLockObject.wait(timeoutMillis);

    }

    /**
     * Waits on the {@link #threadLockObject}
     *
     * See {@link Object#wait(long, int)}
     *
     * @throws InterruptedException
     */
    public void waitOnLock(long timeoutMillis, int nanos) throws InterruptedException {
        threadLockObject.wait(timeoutMillis, nanos);
    }


}
