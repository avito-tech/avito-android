package com.avito.android.graphite

import com.avito.logger.LoggerFactory

internal class GraphiteSenderImpl(
    private val config: GraphiteConfig,
    private val transport: GraphiteTransport,
    loggerFactory: LoggerFactory,
) : GraphiteSender {

    private val logger = loggerFactory.create("Graphite")

    @Throws(Exception::class)
    override fun send(metric: GraphiteMetric) {
        if (!config.isEnabled) return
        sendInternal(metric)
    }

    @Throws(Exception::class)
    private fun sendInternal(metric: GraphiteMetric) {
        val withPrefix = metric.withPrefix(config.metricPrefix)
        transport.send(withPrefix)
        logger.debug(withPrefix.toString())
    }
}
