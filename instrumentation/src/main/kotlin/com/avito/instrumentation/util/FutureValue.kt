package com.avito.instrumentation.util

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

internal interface FutureValue<T> {
    fun get(): T
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
        if (!isSet.get() && isSet.compareAndSet(false, true)) {
            slot = value
            latch.countDown()

            return true
        }

        return false
    }
}
