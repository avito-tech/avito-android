package com.avito.instrumentation

import com.avito.android.runner.report.StubReport
import com.avito.cd.CdBuildResult
import com.avito.instrumentation.internal.GetTestResultsAction
import com.avito.report.ReportViewer
import com.avito.report.StubReportViewer
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.createStubInstance
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class GetTestResultsActionTest {

    @Test
    fun `action - getTestResultAction success`() {
        val reportCoordinates = ReportCoordinates.createStubInstance()
        val reportId = "reportId"
        val report = StubReport().also { report -> report.reportId = reportId }
        val reportViewer = StubReportViewer()
        val results = createGetTestResultsAction(
            report = report,
            reportViewer = reportViewer,
            reportCoordinates = reportCoordinates
        ).getTestResults()

        assertThat(results)
            .isEqualTo(
                CdBuildResult.TestResultsLink(
                    reportId = reportId,
                    reportUrl = reportViewer.byReportCoordinatesUrl.toString(),
                    reportCoordinates = CdBuildResult.TestResultsLink.ReportCoordinates(
                        planSlug = reportCoordinates.planSlug,
                        jobSlug = reportCoordinates.jobSlug,
                        runId = reportCoordinates.runId
                    )
                )
            )
    }

    private fun createGetTestResultsAction(
        reportCoordinates: ReportCoordinates = ReportCoordinates.createStubInstance(),
        report: StubReport = StubReport(),
        reportViewer: ReportViewer = StubReportViewer()
    ): GetTestResultsAction {
        return GetTestResultsAction(
            reportViewerUrl = "xxx",
            reportCoordinates = reportCoordinates,
            legacyReport = report,
            reportViewer = reportViewer,
            gitBranch = "gitBranch",
            gitCommit = "gitCommit"
        )
    }
}
