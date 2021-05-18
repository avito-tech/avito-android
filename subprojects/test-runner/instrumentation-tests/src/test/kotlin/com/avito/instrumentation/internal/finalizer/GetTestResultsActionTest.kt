package com.avito.instrumentation.internal.finalizer

import com.avito.cd.CdBuildResult
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
        val reportViewer = StubReportViewer()
        val results = createGetTestResultsAction(
            reportCoordinates = reportCoordinates,
            reportViewer = reportViewer
        ).getTestResults()

        assertThat(results)
            .isEqualTo(
                CdBuildResult.TestResultsLink(
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
        reportViewer: ReportViewer = StubReportViewer()
    ): GetTestResultsAction {
        return GetTestResultsAction(
            reportViewerUrl = "xxx",
            reportCoordinates = reportCoordinates,
            reportViewer = reportViewer
        )
    }
}
