package com.avito.android.stats

import com.avito.logger.LoggerFactory
import com.avito.logger.create
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
        private val config: StatsDConfig,
        loggerFactory: LoggerFactory
    ) : StatsDSender {

        private val logger = loggerFactory.create<StatsDSender>()

        private val errorHandler = StatsDClientErrorHandler {
            logger.warn("statsd error", it)
        }

        private val client: StatsDClient by lazy {
            when (config) {
                is StatsDConfig.Disabled -> NoOpStatsDClient()
                is StatsDConfig.Enabled -> try {
                    NonBlockingStatsDClient(config.namespace, config.host, config.port, errorHandler)
                } catch (err: Exception) {
                    if (config.host == config.fallbackHost) {
                        NoOpStatsDClient()
                    } else {
                        try {
                            NonBlockingStatsDClient(config.namespace, config.fallbackHost, config.port, errorHandler)
                        } catch (err: Exception) {
                            errorHandler.handle(err)
                            NoOpStatsDClient()
                        }
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

            if (config is StatsDConfig.Enabled) {
                logger.debug("${metric.type}:${config.namespace}.$path:${metric.value}")
            }
        }
    }
}
