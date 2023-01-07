package com.github.fernthedev.lightchat.server.netty

import com.github.fernthedev.lightchat.core.StaticHandler
import com.google.common.base.Stopwatch
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.yield
import java.io.Serializable
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

/**
 * This thread creates and pools keys asynchronously to improve
 * login performance of clients
 *
 */
class KeyThread<T : Serializable>(
    private val generateKey: Supplier<T>,
    initialPoolSize: Int,
    private val runningCondition: Supplier<Boolean>,
) {
    private val keyPool: Queue<T> = LinkedBlockingQueue()
    private val futures: Queue<CompletableDeferred<T>> = LinkedList()

    /**
     * When an object implementing interface `Runnable` is used
     * to create a thread, starting the thread causes the object's
     * `run` method to be called in that separately executing
     * thread.
     *
     *
     * The general contract of the method `run` is that it may
     * take any action whatsoever.
     *
     * @see Thread.run
     */
    suspend fun run() = coroutineScope {
        while (runningCondition.get()) {
            // Only create keys to saturate queue
            if (keyPool.size < poolSize) {
                val stopwatch = Stopwatch.createStarted()

                keyPool.add(generateKey.get())

                stopwatch.stop()
                StaticHandler.core.logger.debug(
                    "Created a key. Took {}ms",
                    stopwatch.elapsed(TimeUnit.MILLISECONDS)
                )
            }
            futures.poll()?.complete(keyPool.remove())
            yield()
        }
    }

    val randomKey: Deferred<T>
        get() {
            return if (keyPool.size > 0) {
                CompletableDeferred(keyPool.remove())
            } else {

                val completableFuture = CompletableDeferred<T>()
                futures.add(completableFuture)

                return completableFuture
            }
        }

    val keysInPool: Int
        get() {
            return keyPool.size
        }


    var poolSize = initialPoolSize
        set(value) {
            require(value > 0) { "Pool size must be greater than 0" }

            field = value
        }
}