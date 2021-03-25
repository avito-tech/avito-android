package com.avito.http.internal

import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.TimeMetric
import com.avito.time.TimeProvider
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException

internal class StatsHttpEventListener(
    private val statsDSender: StatsDSender,
    private val timeProvider: TimeProvider
) : EventListener() {

    var callStarted = 0L

    override fun callStart(call: Call) {
        callStarted = timeProvider.nowInMillis()
    }

    override fun responseHeadersEnd(call: Call, response: Response) {
        val latencyMs = response.receivedResponseAtMillis - response.sentRequestAtMillis
        statsDSender.send(TimeMetric(serviceMetric(call.request()).append("${response.code}"), latencyMs))
    }

    override fun callFailed(call: Call, ioe: IOException) {
        val prefix = serviceMetric(call.request())

        val latencyMs = timeProvider.nowInMillis() - callStarted

        if (ioe is SocketTimeoutException) {
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
                    "You should add okhttp.Request.tag() with additional request information for metrics\n" +
                    "or use Retrofit @Tag annotation"
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
