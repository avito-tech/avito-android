package com.avito.instrumentation

import com.avito.cd.CdBuildResult
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.instrumentation.report.FakeReport
import com.avito.report.FakeReportViewer
import com.avito.report.ReportViewer
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.createStubInstance
import com.avito.utils.logging.FakeCILogger
import com.google.common.truth.Truth
import org.funktionale.tries.Try
import org.junit.jupiter.api.Test

class GetTestResultsActionTest {
    private val ciLogger = FakeCILogger()

    @Test
    fun `action - failed and send critical event for configuration without targets`() {
        val configuration = InstrumentationConfiguration.Data.createStubInstance()

        Try {
            createGetTestResultsAction(configuration).getTestResults()
        }.onSuccess {
            Truth.assertWithMessage("Action must be failed for configuration without targets")
                .fail()
        }

        Truth.assertWithMessage("Empty configuration message reporter")
            .that(ciLogger.criticalHandler.lastMessage)
            .isEqualTo("There are no targets in ${configuration.name} configuration")
    }

    @Test
    fun `action - getTestResultAction success`() {
        val configuration = InstrumentationConfiguration.Data.createStubInstance(
            targets = listOf(TargetConfiguration.Data.createStubInstance())
        )
        val reportCoordinates = ReportCoordinates.createStubInstance()
        val reportId = "reportId"
        val report = FakeReport().also { report -> report.reportId = reportId }
        val reportViewer = FakeReportViewer()
        val results = createGetTestResultsAction(
            report = report,
            reportViewer = reportViewer,
            reportCoordinates = reportCoordinates,
            configurationData = configuration
        ).getTestResults()

        Truth.assertThat(results)
            .isEqualTo(
                CdBuildResult.TestResults(
                    reportId = reportId,
                    reportUrl = reportViewer.byReportCoordinatesUrl.toString(),
                    reportCoordinates = CdBuildResult.TestResults.ReportCoordinates(
                        planSlug = reportCoordinates.planSlug,
                        jobSlug = reportCoordinates.jobSlug,
                        runId = reportCoordinates.runId
                    )
                )
            )

    }

    private fun createGetTestResultsAction(
        configurationData: InstrumentationConfiguration.Data,
        reportCoordinates: ReportCoordinates = ReportCoordinates.createStubInstance(),
        report: FakeReport = FakeReport(),
        reportViewer: ReportViewer = FakeReportViewer()
    ): GetTestResultsAction {
        return GetTestResultsAction(
            reportApiUrl = "xxx",
            reportApiFallbackUrl = "xxx",
            reportViewerUrl = "xxx",
            reportCoordinates = reportCoordinates,
            ciLogger = ciLogger,
            buildId = "buildId",
            report = report,
            reportViewer = reportViewer,
            gitBranch = "gitBranch",
            gitCommit = "gitCommit",
            configuration = configurationData
        )
    }
}
