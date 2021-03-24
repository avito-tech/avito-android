package com.avito.http

import com.avito.http.internal.RequestMetadata
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

public class RequestMetadataInterceptor(private val metadataProvider: (Request) -> RequestMetadata) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val modifiedRequest = request.newBuilder()
            .tag(type = RequestMetadata::class.java, tag = metadataProvider.invoke(request))
            .build()
        return chain.proceed(modifiedRequest)
    }

    public companion object {

        public fun lastPathSegmentAsMethod(serviceName: String): (Request) -> RequestMetadata = { request ->
            val pathSegments = request.url.pathSegments
            val methodName = if (pathSegments.isNotEmpty()) {
                pathSegments.last()
            } else {
                "unknown"
            }
            RequestMetadata(serviceName, methodName)
        }
    }
}
