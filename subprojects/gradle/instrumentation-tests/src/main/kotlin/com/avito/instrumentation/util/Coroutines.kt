package com.avito.instrumentation.util

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch

// TODO: Don't use global scope. Unconfined coroutines lead to leaks
//  Client should be responsible for parallelization or it should provide the scope
suspend fun <T> ReceiveChannel<T>.forEachAsync(action: suspend (T) -> Unit) {
    for (item in this) {
        GlobalScope.launch {
            action(item)
        }
    }
}
