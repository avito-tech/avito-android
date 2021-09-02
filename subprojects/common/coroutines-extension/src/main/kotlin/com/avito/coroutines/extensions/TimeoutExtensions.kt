package com.avito.coroutines.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withTimeoutOrNull

public suspend fun <T> withTimeoutOrDefault(timeMillis: Long, default: T, block: suspend CoroutineScope.() -> T): T {
    return withTimeoutOrNull(timeMillis, block) ?: default
}
