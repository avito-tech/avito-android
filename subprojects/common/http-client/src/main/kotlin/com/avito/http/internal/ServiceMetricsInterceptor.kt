package com.avito.http.internal

import okhttp3.Interceptor
import okhttp3.Response
import okio.IOException
import java.net.SocketTimeoutException

internal class ServiceMetricsInterceptor(private val listener: ServiceEventsListener) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response = try {
        val response = chain.proceed(chain.request())

        val latency = response.receivedResponseAtMillis - response.sentRequestAtMillis

        listener.onResponse(
            code = response.code,
            latencyMs = latency
        )
        response
    } catch (e: IOException) {
        if (e is SocketTimeoutException) {
            listener.onTimeout(e)
        } else {
            listener.onUnknownException(e)
        }
        throw e
    }
}
