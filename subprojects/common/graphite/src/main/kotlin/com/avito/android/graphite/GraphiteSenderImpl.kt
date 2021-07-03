package com.avito.android.graphite

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import java.io.DataOutputStream
import java.net.Socket

internal class GraphiteSenderImpl(
    private val config: GraphiteConfig,
    loggerFactory: LoggerFactory
) : GraphiteSender {

    private val logger = loggerFactory.create<GraphiteSender>()

    override fun send(metric: GraphiteMetric) {
        if (!config.isEnabled) return

        val message = graphiteMessage(config, metric)
        send(message)
    }

    private fun send(message: String) {
        try {
            socket().use { socket ->
                val dos = DataOutputStream(socket.getOutputStream())
                dos.writeBytes(message)
            }
        } catch (e: Exception) {
            logger.warn(message, e)
        }
    }

    /**
     * Returns graphite message in plaintext protocol
     * (https://graphite.readthedocs.io/en/latest/feeding-carbon.html#the-plaintext-protocol)
     */
    private fun graphiteMessage(config: GraphiteConfig, metric: GraphiteMetric): String {
        val metricName = if (config.namespace.isEmpty()) {
            metric.path
        } else {
            config.namespace.removeSuffix(".") + "." + metric.path
        }
        return "$metricName ${metric.value} ${metric.timestamp}\n"
    }

    private fun socket(): Socket = Socket(config.host, config.port)
}
