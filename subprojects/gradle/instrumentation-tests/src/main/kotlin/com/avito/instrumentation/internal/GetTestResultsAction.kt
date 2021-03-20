package com.avito.instrumentation.internal

import com.avito.android.runner.report.Report
import com.avito.cd.CdBuildResult
import com.avito.report.ReportViewer
import com.avito.report.model.ReportCoordinates

/**
 * Этот action может ввести в заблуждение своим названием.
 * Он компонует доступные после этапа конфигурации данные в TestResults,
 * который содержит ссылки на будущие результаты
 */
internal class GetTestResultsAction(
    reportViewerUrl: String,
    private val reportCoordinates: ReportCoordinates,
    private val report: Report,
    private val reportViewer: ReportViewer = ReportViewer.Impl(reportViewerUrl),
    private val gitBranch: String,
    private val gitCommit: String
) {

    fun getTestResults(): CdBuildResult.TestResultsLink {
        return CdBuildResult.TestResultsLink(
            reportId = getReportId(),
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

    private fun getReportId(): String? {
        report.tryCreate(
            testHost = "", // todo
            gitBranch = gitBranch,
            gitCommit = gitCommit
        )

        return report.tryGetId()
    }
}
