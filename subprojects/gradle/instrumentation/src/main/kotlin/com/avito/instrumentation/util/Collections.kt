package com.avito.instrumentation.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

fun <T, R> Collection<T>.iterateInParallel(action: suspend (index: Int, item: T) -> R): Collection<R> {
    val scope = CoroutineScope(
        Executors.newFixedThreadPool(size).asCoroutineDispatcher()
    )

    return this
        .mapIndexed { index, item ->
            scope.async {
                action(index, item)
            }
        }
        .map {
            runBlocking {
                it.await()
            }
        }
}
