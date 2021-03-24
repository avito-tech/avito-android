package com.avito.http.internal

import com.avito.time.TimeProvider
import okhttp3.Interceptor
import okhttp3.Response
import okio.IOException

internal class ServiceMetricsInterceptor(
    private val listener: ServiceEventsListener,
    private val timeProvider: TimeProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()

        val startTime = timeProvider.nowInMillis()

        return try {
            val response = chain.proceed(request)

            listener.onResponse(
                request = request,
                code = response.code,
                latencyMs = response.receivedResponseAtMillis - response.sentRequestAtMillis
            )

            response
        } catch (e: IOException) {
            listener.onException(
                request = request,
                exception = e,
                latencyMs = timeProvider.nowInMillis() - startTime
            )
            throw e
        }
    }
}
