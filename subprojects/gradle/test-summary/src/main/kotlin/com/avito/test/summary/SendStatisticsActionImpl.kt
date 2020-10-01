package com.avito.test.summary

import com.avito.report.ReportsApi
import com.avito.report.model.GetReportResult
import com.avito.report.model.ReportCoordinates
import com.avito.utils.logging.CILogger

internal interface SendStatisticsAction {

    fun send(reportCoordinates: ReportCoordinates)
}

internal class SendStatisticsActionImpl(
    private val reportApi: ReportsApi,
    private val testSummarySender: TestSummarySender,
    private val graphiteRunWriter: GraphiteRunWriter,
    private val ciLogger: CILogger
) : SendStatisticsAction {

    override fun send(reportCoordinates: ReportCoordinates) {
        reportApi.getCrossDeviceTestData(reportCoordinates).fold(
            { suite ->
                //todo return try
                testSummarySender.send(suite, requireNotNull(reportApi.tryGetId(reportCoordinates)))

                val request = graphiteRunWriter.write(suite)
                request.onFailure { throwable ->
                    ciLogger.critical("Can't send test run data to graphite", throwable)
                }
            },
            { throwable -> ciLogger.critical("Can't get suite report", throwable) })
    }

    private fun ReportsApi.tryGetId(reportCoordinates: ReportCoordinates): String? {
        return when (val result = getReport(reportCoordinates)) {
            is GetReportResult.Found -> result.report.id
            GetReportResult.NotFound -> {
                ciLogger.critical("Can't find report for runId=${reportCoordinates.runId}")
                null
            }
            is GetReportResult.Error -> {
                ciLogger.critical("Can't find report for runId=${reportCoordinates.runId}", result.exception)
                null
            }
        }
    }
}
