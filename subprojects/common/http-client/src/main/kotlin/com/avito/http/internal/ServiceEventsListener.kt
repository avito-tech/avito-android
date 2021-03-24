package com.avito.http.internal

import okhttp3.Request
import java.io.IOException

internal interface ServiceEventsListener {

    fun onResponse(request: Request, code: Int, latencyMs: Long)

    fun onException(request: Request, exception: IOException, latencyMs: Long)
}
