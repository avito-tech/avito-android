package com.avito.instrumentation.util

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch

suspend fun <T> Collection<Channel<T>>.merge(): Channel<T> = Channel<T>().apply {
    forEach { channel ->
        GlobalScope.launch {
            for (item in channel) {
                send(item)
            }
        }
    }
}

suspend fun <T> ReceiveChannel<T>.forEachAsync(action: suspend (T) -> Unit) {
    for (item in this) {
        GlobalScope.launch {
            action(item)
        }
    }
}
