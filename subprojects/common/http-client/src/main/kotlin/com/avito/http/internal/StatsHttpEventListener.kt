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
    private val requestMetadataProvider: RequestMetadataProvider,
    loggerFactory: LoggerFactory,
) : EventListener() {

    private val logger = loggerFactory.create<StatsHttpEventListener>()

    var callStarted = 0L

    var responseCode: Int? = null

    override fun callStart(call: Call) {
        callStarted = timeProvider.nowInMillis()
    }

    override fun responseHeadersEnd(call: Call, response: Response) {
        if (response.isSuccessful) {
            responseCode = response.code
        } else {
            sendCode(
                call = call,
                code = response.code.toString(),
                latencyMs = response.receivedResponseAtMillis - response.sentRequestAtMillis
            )
        }
    }

    override fun callEnd(call: Call) {
        if (responseCode in 200..299) {
            val latencyMs = timeProvider.nowInMillis() - callStarted
            sendCode(call, responseCode.toString(), latencyMs)
        }
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

    private fun sendCode(call: Call, code: String, latencyMs: Long) {
        val prefix = serviceMetric(call.request())
        statsDSender.send(TimeMetric(prefix.append(code), latencyMs))
    }

    private fun serviceMetric(request: Request): SeriesName {
        return requestMetadataProvider.provide(request).fold(
            onSuccess = { metadata ->
                SeriesName.create(
                    "service",
                    metadata.serviceName,
                    metadata.methodName
                )
            },
            onFailure = { throwable ->
                val urlWithoutParams = request.url.newBuilder()
                    .query(null)
                    .build()

                val seriesName = SeriesName.create(
                    "service",
                    "unknown-service",
                    "unknown-method"
                )

                logger.warn(
                    msg = "RequestMetadata not available for: $urlWithoutParams\n" +
                        "Metrics will be send as: $seriesName\n" +
                        "You should add okhttp.Request.tag() with additional request information for metrics\n" +
                        "or use Retrofit @Tag annotation,\n" +
                        "or use custom RequestMetadataProvider " +
                        "if original request creation not available for modification",
                    error = throwable
                )
                seriesName
            }
        )
    }
}
