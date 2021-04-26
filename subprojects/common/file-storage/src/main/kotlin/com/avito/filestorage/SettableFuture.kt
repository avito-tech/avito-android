package com.avito.filestorage

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

public interface FutureValue<T> {

    public fun get(): T

    public fun <T, R> FutureValue<T>.map(transformer: (T) -> R): FutureValue<R> = object : FutureValue<R> {
        override fun get(): R = transformer(this@map.get())
    }

    public companion object {

        public fun <T> create(stubValue: T): FutureValue<T> = object : FutureValue<T> {
            override fun get(): T = stubValue
        }
    }
}

internal class SettableFutureValue<T> : FutureValue<T> {
    private val latch = CountDownLatch(1)
    private val isSet = AtomicBoolean()
    private var slot: T? = null

    override fun get(): T {
        latch.await()
        return slot!!
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
