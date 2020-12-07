package com.avito.runner.test.mock

sealed class MockActionResult<T> {

    abstract fun get(): T

    class Success<T>(private val result: T) : MockActionResult<T>() {
        override fun get(): T = result
    }

    class Failed<T>(private val t: Throwable) : MockActionResult<T>() {
        override fun get(): T {
            throw t
        }
    }
}
