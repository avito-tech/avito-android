package com.avito.instrumentation.report

import com.avito.report.ReportsApi
import com.avito.report.model.ReportCoordinates
import com.avito.test.summary.GraphiteRunWriter
import com.avito.test.summary.TestSummarySender
import com.avito.utils.logging.CILogger

class SendStatisticsAction(
    private val reportId: String,
    private val reportCoordinates: ReportCoordinates,
    private val testSummarySender: TestSummarySender,
    private val reportsApi: ReportsApi,
    private val graphiteRunWriter: GraphiteRunWriter,
    private val ciLogger: CILogger
) {

    fun send() {
        reportsApi.getCrossDeviceTestData(reportCoordinates).fold(
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
