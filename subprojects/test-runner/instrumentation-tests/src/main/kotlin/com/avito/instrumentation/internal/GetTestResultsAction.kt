package com.avito.instrumentation.internal

import com.avito.cd.CdBuildResult
import com.avito.reportviewer.ReportViewer
import com.avito.reportviewer.model.ReportCoordinates

/**
 * Used for CD contract
 */
internal class GetTestResultsAction(
    reportViewerUrl: String,
    private val reportCoordinates: ReportCoordinates,
    private val reportViewer: ReportViewer = ReportViewer.Impl(reportViewerUrl, reportCoordinates)
) {

    fun getTestResults(): CdBuildResult.TestResultsLink {
        return CdBuildResult.TestResultsLink(
            reportUrl = getReportUrl(reportCoordinates),
            reportCoordinates = CdBuildResult.TestResultsLink.ReportCoordinates(
                planSlug = reportCoordinates.planSlug,
                jobSlug = reportCoordinates.jobSlug,
                runId = reportCoordinates.runId
            )
        )
    }

    private fun getReportUrl(reportCoordinates: ReportCoordinates): String {
        return reportViewer.generateReportUrl(reportCoordinates, false).toString()
    }
}
