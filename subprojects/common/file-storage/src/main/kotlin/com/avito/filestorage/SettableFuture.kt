package com.avito.filestorage

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

interface FutureValue<T> {
    fun get(): T
}

internal class SettableFutureValue<T> : FutureValue<T> {
    private val latch = CountDownLatch(1)
    private val isSet = AtomicBoolean()
    private var slot: T? = null

    override fun get(): T {
        latch.await(5, TimeUnit.SECONDS)
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

fun <T, R> FutureValue<T>.map(transformer: (T) -> R): FutureValue<R> = object : FutureValue<R> {
    override fun get(): R = transformer(this@map.get())
}
