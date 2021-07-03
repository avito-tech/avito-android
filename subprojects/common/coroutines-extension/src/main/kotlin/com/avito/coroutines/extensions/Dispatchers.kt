package com.avito.coroutines.extensions

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

public interface Dispatchers {

    public fun dispatcher(): CoroutineDispatcher

    public object SingleThread : Dispatchers {

        override fun dispatcher(): ExecutorCoroutineDispatcher {
            return Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        }
    }
}
