package com.github.fernthedev.lightchat.core.api

import lombok.Getter

class ThreadLock {
    private val threadLockObject = Any()

    @Getter
    private var lock = false
    fun lock() {
        lock = true
    }

    /**
     * Notifies all threads that the object is ready
     *
     * See [Object.notifyAll]
     *
     */
    fun notifyAllThreads() {
        lock = false
        synchronized(threadLockObject) {
//            threadLockObject.notifyAll()
        }
    }

    /**
     * Waits on the [.threadLockObject]
     *
     * See [Object.wait]
     *
     * @throws InterruptedException
     */
    @Throws(InterruptedException::class)
    fun waitOnLock() {
        synchronized(threadLockObject) {
//            threadLockObject.wait()
        }
    }

    /**
     * Waits on the [.threadLockObject]
     *
     * See [Object.wait]
     *
     * @throws InterruptedException
     */
    @Throws(InterruptedException::class)
    fun waitOnLock(timeoutMillis: Long) {
        synchronized(threadLockObject) {
//            threadLockObject.wait(timeoutMillis)
        }
    }

    /**
     * Waits on the [.threadLockObject]
     *
     * See [Object.wait]
     *
     * @throws InterruptedException
     */
    @Throws(InterruptedException::class)
    fun waitOnLock(timeoutMillis: Long, nanos: Int) {
        synchronized(threadLockObject) {
//            threadLockObject.wait(timeoutMillis, nanos)
        }
    }
}