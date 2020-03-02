package com.avito.android.graphite

import java.io.DataOutputStream
import java.net.Socket


interface GraphiteSender {

    /**
     * Incubating
     */
    fun send(metric: GraphiteMetric)

    class Impl(
        private val config: GraphiteConfig,
        private val logger: (String, Throwable?) -> Unit
    ) : GraphiteSender {

        override fun send(metric: GraphiteMetric) {
            val metricName = if (config.namespace.isEmpty()) {
                metric.path
            } else {
                config.namespace.removeSuffix(".") + "." + metric.path
            }

            send(metricName, metric.value, metric.timestamp)
        }

        private fun send(path: String, value: String, timestamp: Long) {
            // https://graphite.readthedocs.io/en/latest/feeding-carbon.html#the-plaintext-protocol
            val message = "$path $value $timestamp\n"

            logger.invoke("graphite: $message", null)

            if (!config.isEnabled) return

            try {
                socket().use { socket ->
                    val dos = DataOutputStream(socket.getOutputStream())
                    dos.writeBytes(message)
                }
            } catch (e: Exception) {
                logger("graphite error: $message", e)
            }
        }

        private fun socket(): Socket = Socket(config.host, config.port)
    }
}
