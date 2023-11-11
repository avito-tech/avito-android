package com.avito.runner.scheduler.report

import com.avito.report.Report
import com.avito.report.inmemory.InMemoryReportFactory
import com.avito.reportviewer.ReportsApiFactory
import com.avito.runner.config.RunnerReportConfig
import com.avito.runner.listener.ArtifactsTestListenerProvider
import com.avito.runner.listener.ReportArtifactsTestListenerProvider
import com.avito.runner.listener.TestListenerFactory

internal class ReportViewerReportModule(
    private val dependencies: ReportModuleDependencies,
    private val reportConfig: RunnerReportConfig.ReportViewer,
) : ReportModule {

    override val report: Report = ReportImpl(
        inMemoryReport = InMemoryReportFactory(
            timeProvider = dependencies.timeProvider,
            loggerFactory = dependencies.loggerFactory,
        ).createReport(),
        externalReportService = createAvitoReport()
    )

    override val artifactsTestListenerProvider: ReportArtifactsTestListenerProvider =
        ArtifactsTestListenerProvider(
            testListenerFactory = TestListenerFactory(
                loggerFactory = dependencies.loggerFactory,
                timeProvider = dependencies.timeProvider,
                httpClientBuilder = dependencies.httpClientBuilder
            ),
            loggerFactory = dependencies.loggerFactory,
            testRunnerOutputDir = dependencies.testRunnerOutputDir,
            tempLogcatDir = dependencies.tempLogcatDir,
            report = report,
            reportConfig = reportConfig,
            proguardMappings = dependencies.params.proguardMappings,
            saveTestArtifactsToOutputs = dependencies.params.saveTestArtifactsToOutputs,
            macroBenchmarkOutputs = dependencies.params.macrobenchmarkOutputDir,
        )

    private fun createAvitoReport(): AvitoReport {
        return AvitoReport(
            reportsApi = ReportsApiFactory.create(
                host = reportConfig.reportApiUrl,
                builder = dependencies.httpClientBuilder,
                loggerFactory = dependencies.loggerFactory,
            ),
            reportViewerUrl = reportConfig.reportViewerUrl,
            loggerFactory = dependencies.loggerFactory,
            reportCoordinates = reportConfig.coordinates,
            buildId = dependencies.params.buildId,
            timeProvider = dependencies.timeProvider
        )
    }
}
