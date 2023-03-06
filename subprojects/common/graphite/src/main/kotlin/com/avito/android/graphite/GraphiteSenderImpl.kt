package com.avito.android.graphite

import com.avito.logger.LoggerFactory
import java.io.DataOutputStream
import java.net.Socket

internal class GraphiteSenderImpl(
    private val config: GraphiteConfig,
    loggerFactory: LoggerFactory,
) : GraphiteSender {

    private val logger = loggerFactory.create("Graphite")

    @Throws(Exception::class)
    override fun send(metric: GraphiteMetric) {
        if (!config.isEnabled) return
        sendInternal(metric)
    }

    /**
     * Sends graphite message in plaintext protocol
     * (https://graphite.readthedocs.io/en/latest/feeding-carbon.html#the-plaintext-protocol)
     */
    @Throws(Exception::class)
    private fun sendInternal(metric: GraphiteMetric) {
        val metricPath = config.metricPrefix.append(metric.path)
        val metricRaw = "${metricPath.asAspect()} ${metric.value} ${metric.timeInSec}\n"
        logger.debug(metricRaw)
        socket().use { socket ->
            val dos = DataOutputStream(socket.getOutputStream())
            dos.writeBytes(metricRaw)
        }
    }

    private fun socket(): Socket = Socket(config.host, config.port)
}
