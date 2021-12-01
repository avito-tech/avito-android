package com.avito.test.summary

import com.avito.reportviewer.ReportsApi
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.time.TimeProvider
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

/**
 * TMS will use reports marked with special analyzer_key as source of truth
 *
 * related tasks:
 * CTHL-495
 * MBS-6483
 */
internal class MarkReportAsSourceAction(
    private val reportsApi: ReportsApi,
    private val timeProvider: TimeProvider,
) {

    fun mark(reportCoordinates: ReportCoordinates) {
        val testSuiteVersion = timeProvider.nowInMillis()

        reportsApi.getReport(reportCoordinates)
            .map { report ->
                val preparedData = JsonObject()
                preparedData.add("full", JsonPrimitive(true))
                preparedData.add("version", JsonPrimitive(testSuiteVersion))

                reportsApi.pushPreparedData(
                    reportId = report.id,
                    analyzerKey = "test_suite",
                    preparedData = preparedData
                )
                report
            }
            .map { report ->
                reportsApi.setFinished(reportCoordinates)
                report
            }
            .onFailure {
                // TODO handle throwable
            }
    }
}
