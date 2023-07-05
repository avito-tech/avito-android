package com.avito.android.graphite

import com.avito.graphite.series.SeriesName
import com.avito.logger.PrintlnLoggerFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RealGraphiteTransportTest {

    @Test
    fun `ignoreExceptions is true - success`() {
        createTransport(true).send(createStubMetric())
    }

    @Test
    fun `ignoreExceptions is false - fail`() {
        Assertions.assertThrows(Throwable::class.java) {
            createTransport(false).send(createStubMetric())
        }
    }

    private fun createStubMetric() = GraphiteMetric(SeriesName.create(), "")

    private fun createTransport(ignoreExceptions: Boolean) = GraphiteTransport.Real(
        "stub", 0, ignoreExceptions, PrintlnLoggerFactory,
    )
}
