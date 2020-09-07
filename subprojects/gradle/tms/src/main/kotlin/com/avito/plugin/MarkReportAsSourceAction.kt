package com.avito.plugin

import com.avito.logger.Logger
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
    private val logger: Logger
) {

    fun mark(reportCoordinates: ReportCoordinates) {
        val testSuiteVersion = timeProvider.nowInMillis()

        logger.debug("This is a new version [$testSuiteVersion] of full test suite for tms")

        val reportId = tryGetId(reportCoordinates)

        if (reportId != null) {
            reportsApi.pushPreparedData(
                reportId = reportId,
                analyzerKey = "test_suite",
                preparedData = jsonObject(
                    "full" to true,
                    "version" to testSuiteVersion
                )
            ).onFailure { error ->
                logger.critical("Can't push prepared data: testSuite info", error)
            }

            reportsApi.setFinished(reportCoordinates).onFailure { error ->
                logger.critical("Can't finish report for coordinates: $reportCoordinates", error)
            }
        }
    }

    private fun tryGetId(reportCoordinates: ReportCoordinates): String? {
        return when (val result = reportsApi.getReport(reportCoordinates)) {
            is GetReportResult.Found -> result.report.id
            GetReportResult.NotFound -> {
                logger.warn("Can't find report for runId=${reportCoordinates.runId}")
                null
            }
            is GetReportResult.Error -> {
                logger.critical("Can't find report for runId=${reportCoordinates.runId}", result.exception)
                null
            }
        }
    }
}
