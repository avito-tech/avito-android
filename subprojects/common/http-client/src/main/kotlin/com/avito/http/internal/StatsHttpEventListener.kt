package com.avito.http.internal

import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.TimeMetric
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.time.TimeProvider
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException

internal class StatsHttpEventListener(
    private val statsDSender: StatsDSender,
    private val timeProvider: TimeProvider,
    loggerFactory: LoggerFactory
) : EventListener() {

    private val logger = loggerFactory.create<StatsHttpEventListener>()

    var callStarted = 0L

    override fun callStart(call: Call) {
        callStarted = timeProvider.nowInMillis()
    }

    override fun responseHeadersEnd(call: Call, response: Response) {
        val prefix = serviceMetric(call.request())
        if (prefix != null) {
            val latencyMs = response.receivedResponseAtMillis - response.sentRequestAtMillis
            statsDSender.send(TimeMetric(prefix.append("${response.code}"), latencyMs))
        }
    }

    override fun callFailed(call: Call, ioe: IOException) {
        val prefix = serviceMetric(call.request())

        if (prefix != null) {
            val latencyMs = timeProvider.nowInMillis() - callStarted

            if (ioe is SocketTimeoutException) {
                statsDSender.send(TimeMetric(prefix.append("timeout"), latencyMs))
            } else {
                statsDSender.send(TimeMetric(prefix.append("unknown"), latencyMs))
            }
        }
    }

    private fun serviceMetric(request: Request): SeriesName? {
        val metadata = request.tag(RequestMetadata::class.java)

        return if (metadata == null) {
            logger.warn(
                msg = "RequestMetadata not available\n" +
                    "You should add okhttp.Request.tag() with additional request information for metrics\n" +
                    "or use Retrofit @Tag annotation",
                error = null
            )
            null
        } else {
            SeriesName.create(
                "service",
                metadata.serviceName,
                metadata.methodName
            )
        }
    }
}
