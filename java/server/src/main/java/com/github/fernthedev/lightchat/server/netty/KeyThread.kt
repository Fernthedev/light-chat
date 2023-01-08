package com.github.fernthedev.lightchat.server.netty

import com.github.fernthedev.lightchat.core.StaticHandler
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.yield
import java.io.Serializable
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.function.Supplier
import kotlin.system.measureTimeMillis

/**
 * This thread creates and pools keys asynchronously to improve
 * login performance of clients
 *
 */
class KeyThread<T : Serializable>(
    private val generateKey: Supplier<T>,
    initialPoolSize: Int,
    private val runningCondition: () -> Boolean,
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
        while (runningCondition()) {
            // Only create keys to saturate queue
            if (keyPool.size >= poolSize) {
                yield()
                continue
            }

            val time = measureTimeMillis {
                keyPool.add(generateKey.get())
            }
            StaticHandler.core.logger.debug(
                "Created a key. Took {}ms",
                time
            )
            futures.poll()?.complete(keyPool.remove())
        }
        StaticHandler.core.logger.debug("Shutting down RSA key thread")
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