package com.avito.android.stats

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.timgroup.statsd.NoOpStatsDClient
import com.timgroup.statsd.NonBlockingStatsDClient
import com.timgroup.statsd.StatsDClient
import com.timgroup.statsd.StatsDClientErrorHandler

public interface StatsDSender {

    public fun send(metric: StatsMetric)

    @Deprecated("Use send() without prefix. This will be deleted")
    public fun send(prefix: SeriesName, metric: StatsMetric)

    // todo extract and make internal after next release (used in avito directly)
    public class Impl(
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
                    NonBlockingStatsDClient(
                        config.namespace.toString(),
                        config.host,
                        config.port,
                        errorHandler
                    )
                } catch (e: Exception) {
                    logger.warn("Can't create statsDClient on main host", e)
                    if (config.host == config.fallbackHost) {
                        NoOpStatsDClient()
                    } else {
                        try {
                            NonBlockingStatsDClient(
                                config.namespace.toString(),
                                config.fallbackHost,
                                config.port,
                                errorHandler
                            )
                        } catch (err: Exception) {
                            errorHandler.handle(err)
                            NoOpStatsDClient()
                        }
                    }
                }
            }
        }

        override fun send(metric: StatsMetric) {
            sendInternal(prefix = null, metric)
        }

        @Suppress("OverridingDeprecatedMember")
        override fun send(prefix: SeriesName, metric: StatsMetric) {
            sendInternal(prefix, metric)
        }

        private fun sendInternal(prefix: SeriesName?, metric: StatsMetric) {

            val aspect: String = if (prefix != null) {
                metric.name.prefix(prefix)
            } else {
                metric.name
            }.toString()

            @Suppress("DEPRECATION")
            when (metric) {
                is TimeMetric -> client.time(aspect, metric.value)
                is CountMetric -> client.count(aspect, metric.value)
                is GaugeLongMetric -> client.gauge(aspect, metric.value)
                is GaugeDoubleMetric -> client.gauge(aspect, metric.value)
            }

            if (config is StatsDConfig.Enabled) {
                logger.debug("${metric.type}:${config.namespace}.$aspect:${metric.value}")
            } else {
                logger.debug("Skip sending event: ${metric.type}:<namespace>.$aspect:${metric.value}")
            }
        }
    }

    public companion object {

        public fun create(
            config: StatsDConfig,
            loggerFactory: LoggerFactory
        ): StatsDSender {
            return Impl(config, loggerFactory)
        }
    }
}
