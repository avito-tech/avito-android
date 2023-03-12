package com.avito.android.graphite

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
    ) : GraphiteTransport {

        /**
         * Sends graphite message in plaintext protocol
         * (https://graphite.readthedocs.io/en/latest/feeding-carbon.html#the-plaintext-protocol)
         */
        override fun send(metric: GraphiteMetric) {
            val metricRaw = metric.toRaw()
            socket().use { socket ->
                val dos = DataOutputStream(socket.getOutputStream())
                dos.writeBytes(metricRaw)
            }
        }

        private fun socket(): Socket = Socket(host, port)
    }
}
