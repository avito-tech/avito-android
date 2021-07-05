package com.avito.instrumentation.internal

import com.avito.cd.CdBuildResult
import com.avito.reportviewer.ReportViewerLinksGenerator
import com.avito.reportviewer.StubReportViewerLinksGenerator
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.reportviewer.model.createStubInstance
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class GetTestResultsActionTest {

    @Test
    fun `action - getTestResultAction success`() {
        val reportCoordinates = ReportCoordinates.createStubInstance()
        val reportViewer = StubReportViewerLinksGenerator()
        val results = createGetTestResultsAction(
            reportCoordinates = reportCoordinates,
            reportViewerLinksGenerator = reportViewer
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
        reportViewerLinksGenerator: ReportViewerLinksGenerator = StubReportViewerLinksGenerator()
    ): GetTestResultsAction {
        return GetTestResultsAction(
            reportCoordinates = reportCoordinates,
            reportLinksGenerator = reportViewerLinksGenerator
        )
    }
}
