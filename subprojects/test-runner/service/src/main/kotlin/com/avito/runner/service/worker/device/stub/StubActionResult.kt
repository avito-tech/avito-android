package com.avito.runner.service.worker.device.stub

public sealed class StubActionResult<T> {

    public abstract fun get(): T

    public class Success<T>(private val result: T) : StubActionResult<T>() {
        override fun get(): T = result
    }

    public class Failed<T>(private val t: Throwable) : StubActionResult<T>() {
        override fun get(): T {
            throw t
        }
    }
}
