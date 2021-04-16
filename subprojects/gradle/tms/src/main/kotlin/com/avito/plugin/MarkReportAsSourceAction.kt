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

        reportsApi.getReport(reportCoordinates).fold(
            onSuccess = { report ->
                reportsApi.pushPreparedData(
                    reportId = report.id,
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
                                        "with id: ${report.id}, " +
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
            },
            onFailure = { throwable ->
                logger.critical("Can't find report for runId=${reportCoordinates.runId}", throwable)
            }
        )
    }
}
