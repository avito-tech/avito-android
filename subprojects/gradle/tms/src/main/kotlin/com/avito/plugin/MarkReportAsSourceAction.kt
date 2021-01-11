package com.avito.plugin

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.ReportsApi
import com.avito.report.model.GetReportResult
import com.avito.report.model.ReportCoordinates
import com.avito.time.TimeProvider
import com.github.salomonbrys.kotson.jsonObject

/**
 * TMS will use reports marked with special analyzer_key as source of truth
 *
 * related tasks:
 * CTHL-495
 * MBS-6483
 */
class MarkReportAsSourceAction(
    private val reportsApi: ReportsApi,
    private val timeProvider: TimeProvider,
    loggerFactory: LoggerFactory
) {

    private val logger = loggerFactory.create<MarkReportAsSourceAction>()

    fun mark(reportCoordinates: ReportCoordinates) {
        val testSuiteVersion = timeProvider.nowInMillis()

        when (val result = reportsApi.getReport(reportCoordinates)) {
            is GetReportResult.Found ->
                reportsApi.pushPreparedData(
                    reportId = result.report.id,
                    analyzerKey = "test_suite",
                    preparedData = jsonObject(
                        "full" to true,
                        "version" to testSuiteVersion
                    )
                ).fold(
                    {
                        reportsApi.setFinished(reportCoordinates).fold(
                            {
                                logger.info(
                                    "Test suite for tms version $testSuiteVersion, " +
                                        "with id: ${result.report.id}, " +
                                        "coordinates: $reportCoordinates marked as source of truth for tms"
                                )
                            },
                            { error ->
                                logger.critical("Can't finish report for coordinates: $reportCoordinates", error)
                            }
                        )
                    },
                    { error ->
                        logger.critical("Can't push prepared data: testSuite info", error)
                    }
                )
            GetReportResult.NotFound ->
                logger.critical(
                    "Can't get reportId for coordinates: $reportCoordinates",
                    IllegalStateException("stub")
                )
            is GetReportResult.Error ->
                logger.critical("Can't find report for runId=${reportCoordinates.runId}", result.exception)
        }
    }
}
