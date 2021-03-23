package com.avito.http.internal

import java.io.IOException
import java.net.SocketTimeoutException

internal interface ServiceEventsListener {

    fun onResponse(code: Int, latencyMs: Long)

    fun onTimeout(e: SocketTimeoutException)

    fun onUnknownException(e: IOException)
}
