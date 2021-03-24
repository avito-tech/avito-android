package com.avito.http.internal

import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.TimeMetric
import okhttp3.Request
import java.io.IOException
import java.net.SocketTimeoutException

internal class StatsdServiceEventsListener(
    private val statsDSender: StatsDSender
) : ServiceEventsListener {

    override fun onResponse(request: Request, code: Int, latencyMs: Long) {
        statsDSender.send(TimeMetric(serviceMetric(request).append("$code"), latencyMs))
    }

    override fun onException(request: Request, exception: IOException, latencyMs: Long) {
        val prefix = serviceMetric(request)

        if (exception is SocketTimeoutException) {
            statsDSender.send(TimeMetric(prefix.append("timeout"), latencyMs))
        } else {
            statsDSender.send(TimeMetric(prefix.append("unknown"), latencyMs))
        }
    }

    private fun serviceMetric(request: Request): SeriesName {
        val metadata = request.tag(RequestMetadata::class.java)

        if (metadata == null) {
            throw IllegalStateException(
                "RequestMetadata not available\n" +
                    "You should add request.tag() with additional request information for metrics\n" +
                    "Check RequestMetadataInterceptor implementation"
            )
        } else {
            return SeriesName.create(
                "service",
                metadata.serviceName,
                metadata.methodName
            )
        }
    }
}
