package com.avito.android.stats

import com.timgroup.statsd.NoOpStatsDClient
import com.timgroup.statsd.NonBlockingStatsDClient
import com.timgroup.statsd.StatsDClient
import com.timgroup.statsd.StatsDClientErrorHandler

/**
 * Use [Project.statsd] to gain instance
 */
interface StatsDSender {

    fun send(prefix: String = "", metric: StatsMetric)

    class Impl(
        suppress: Boolean,
        host: String,
        fallbackHost: String,
        port: Int,
        private val namespace: String,
        private val logger: (String, Throwable?) -> Unit
    ) : StatsDSender {

        private val client: StatsDClient by lazy {
            if (suppress) {
                NoOpStatsDClient()
            } else {
                try {
                    NonBlockingStatsDClient(namespace, host, port, errorHandler)
                } catch (err: Exception) {
                    try {
                        NonBlockingStatsDClient(namespace, fallbackHost, port, errorHandler)
                    } catch (err: Exception) {
                        errorHandler.handle(err)
                        NoOpStatsDClient()
                    }
                }
            }
        }

        override fun send(prefix: String, metric: StatsMetric) {
            val path = if (prefix.isNotBlank()) {
                prefix + "." + metric.path
            } else {
                metric.path
            }
            @Suppress("USELESS_CAST")
            when (metric) {
                is TimeMetric -> client.time(path, metric.value)
                is CountMetric -> client.count(path, metric.value)
                is GaugeMetric -> client.gauge(path, metric.value)
            } as Unit
            logger.invoke("statsd:${metric.type}:$namespace.$path:${metric.value}", null)
        }

        private val errorHandler = StatsDClientErrorHandler {
            logger.invoke("statsd", it)
        }
    }
}

