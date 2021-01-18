package com.avito.android.elastic

import com.avito.logger.Logger
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

internal class RoundRobinInterceptor(
    val roundRobin: ThreadLocal<RoundRobinIterable<HttpUrl>>,
    val logger: Logger
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val request = requestOnNextUrl(originalRequest, roundRobin.get().next())
        return chain.proceed(request)
    }

    companion object {
        internal fun requestOnNextUrl(
            originalRequest: Request,
            nextUrl: HttpUrl
        ): Request {
            return originalRequest.newBuilder()
                .url(
                    originalRequest.url.newBuilder()
                        .scheme(nextUrl.scheme)
                        .host(nextUrl.host)
                        .port(nextUrl.port)
                        .build()
                )
                .build()
        }
    }
}
