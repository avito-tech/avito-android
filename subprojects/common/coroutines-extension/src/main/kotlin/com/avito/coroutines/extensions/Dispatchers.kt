package com.avito.coroutines.extensions

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

interface Dispatchers {

    fun dispatcher(): CoroutineDispatcher

    object SingleThread : Dispatchers {
        override fun dispatcher() = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    }
}
