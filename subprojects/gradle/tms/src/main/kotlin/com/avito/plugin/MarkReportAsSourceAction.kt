package com.avito.plugin

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.ReportsApi
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

        reportsApi.getReport(reportCoordinates)
            .map { report ->
                reportsApi.pushPreparedData(
                    reportId = report.id,
                    analyzerKey = "test_suite",
                    preparedData = jsonObject(
                        "full" to true,
                        "version" to testSuiteVersion
                    )
                )
                report
            }
            .map { report ->
                reportsApi.setFinished(reportCoordinates)
                report
            }
            .onSuccess { report ->
                logger.info(
                    "Test suite for tms version $testSuiteVersion, " +
                        "with id: ${report.id}, " +
                        "coordinates: $reportCoordinates marked as source of truth for tms"
                )
            }
            .onFailure { throwable ->
                logger.critical("Can't mark test suite for tms; runId=${reportCoordinates.runId}", throwable)
            }
    }
}
