package com.avito.android.graphite

import com.avito.logger.LoggerFactory
import java.io.DataOutputStream
import java.net.Socket

internal interface GraphiteTransport {

    fun send(metric: GraphiteMetric)

    fun GraphiteMetric.toRaw(): String {
        return "${path.asAspect()} $value $timeInSec\n"
    }

    class Test : GraphiteTransport {
        override fun send(metric: GraphiteMetric) {
            println("graphite-test ${metric.toRaw()}")
        }
    }

    class Real(
        private val host: String,
        private val port: Int,
        private val ignoreExceptions: Boolean,
        loggerFactory: LoggerFactory,
    ) : GraphiteTransport {

        private val logger = loggerFactory.create("GraphiteTransport.Real")

        /**
         * Sends graphite message in plaintext protocol
         * (https://graphite.readthedocs.io/en/latest/feeding-carbon.html#the-plaintext-protocol)
         */
        override fun send(metric: GraphiteMetric) {
            val metricRaw = metric.toRaw()
            val socket = createSocketSafe()
            when {
                socket != null -> socket.use {
                    val dos = DataOutputStream(it.getOutputStream())
                    dos.writeBytes(metricRaw)
                }
                ignoreExceptions -> logger.warn("Fail to create Socket. Metric $metric lost")
                else -> throw IllegalStateException("Fail to create Socket. See logs to find details")
            }
        }

        private fun createSocketSafe(): Socket? {
            var socket: Socket? = null
            for (tryIndex in 0..3) {
                if (socket != null) {
                    break
                }
                socket = try {
                    Socket(host, port)
                } catch (e: Throwable) {
                    logger.warn("Fail attempt $tryIndex to create Socket", e)
                    null
                }
            }
            return socket
        }
    }
}
