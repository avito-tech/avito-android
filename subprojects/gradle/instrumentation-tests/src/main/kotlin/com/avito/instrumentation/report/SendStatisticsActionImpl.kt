package com.avito.instrumentation.report

import com.avito.test.summary.GraphiteRunWriter
import com.avito.test.summary.TestSummarySender
import com.avito.utils.logging.CILogger

interface SendStatisticsAction {
    fun send()
}

class SendStatisticsActionImpl(
    private val report: Report,
    private val testSummarySender: TestSummarySender,
    private val graphiteRunWriter: GraphiteRunWriter,
    private val ciLogger: CILogger
) : SendStatisticsAction {

    override fun send() {
        report.getCrossDeviceTestData().fold(
            { suite ->
                //todo return try
                testSummarySender.send(suite, requireNotNull(report.tryGetId()))

                val request = graphiteRunWriter.write(suite)
                request.onFailure { throwable ->
                    ciLogger.critical("Can't send test run data to graphite", throwable)
                }
            },
            { throwable -> ciLogger.critical("Can't get suite report", throwable) })
    }
}
