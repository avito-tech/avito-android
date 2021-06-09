package com.avito.filestorage

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

public interface FutureValue<T> {

    /**
     * Block current Thread until Future is ready
     */
    public fun get(): T

    /**
     * Immediately returns. Used from coroutines
     * @return null if Future isn't ready
     */
    public fun tryGet(): T?

    public fun <T, R> FutureValue<T>.map(transformer: (T) -> R): FutureValue<R> = object : FutureValue<R> {
        override fun get(): R = transformer(this@map.get())
        override fun tryGet(): R? = this@map.tryGet()?.let { transformer(it) }
    }

    public companion object {

        public fun <T> create(stubValue: T): FutureValue<T> = object : FutureValue<T> {
            override fun get(): T = stubValue
            override fun tryGet(): T? = stubValue
        }
    }
}

internal class SettableFutureValue<T> : FutureValue<T> {
    private val latch = CountDownLatch(1)
    private val isSet = AtomicBoolean()
    @Volatile
    private var slot: T? = null

    override fun get(): T {
        latch.await()
        return slot!!
    }

    override fun tryGet(): T? {
        return slot
    }

    fun set(value: T): Boolean {
        if (isSet.compareAndSet(false, true)) {
            slot = value
            latch.countDown()

            return true
        }

        return false
    }
}
