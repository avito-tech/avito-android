package com.avito.http

import okhttp3.Request

public data class RetryPolicy(
    val tries: Int = 5,
    val allowedMethods: List<String> = listOf("GET"),
    val allowedCodes: List<Int> = listOf(
        HttpCodes.CLIENT_TIMEOUT,
        HttpCodes.INTERNAL_ERROR,
        HttpCodes.BAD_GATEWAY,
        HttpCodes.UNAVAILABLE,
        HttpCodes.GATEWAY_TIMEOUT
    ),
    val delayBetweenTriesMs: Long = 1000,
    val useIncreasingDelay: Boolean = true,
    val modifyRetryRequest: (Request) -> Request = { it },
    val onTryFail: TryFailCallback = TryFailCallback.STUB
)
