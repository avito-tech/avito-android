package com.avito.instrumentation.report

import com.avito.test.summary.GraphiteRunWriter
import com.avito.test.summary.TestSummarySender
import com.avito.utils.logging.CILogger

class SendStatisticsAction(
    private val reportId: String, // todo get from report
    private val report: Report,
    private val testSummarySender: TestSummarySender,
    private val graphiteRunWriter: GraphiteRunWriter,
    private val ciLogger: CILogger
) {

    fun send() {
        report.getCrossDeviceTestData().fold(
            { suite ->
                //todo return try
                testSummarySender.send(suite, reportId)

                graphiteRunWriter.write(suite).onFailure { throwable ->
                    ciLogger.critical("Can't send test run data to graphite", throwable)
                }
            },
            { throwable -> ciLogger.critical("Can't get suite report", throwable) })
    }
}
