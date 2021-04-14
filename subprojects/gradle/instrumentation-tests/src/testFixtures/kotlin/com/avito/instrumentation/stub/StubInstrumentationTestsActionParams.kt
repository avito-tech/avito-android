package com.avito.instrumentation.stub

import com.avito.android.runner.report.LegacyReport
import com.avito.android.runner.report.ReadReport
import com.avito.android.runner.report.Report
import com.avito.android.runner.report.StubReport
import com.avito.android.runner.report.factory.LegacyReportFactory
import com.avito.android.stats.StatsDConfig
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.createStubInstance
import com.avito.instrumentation.internal.InstrumentationTestsAction
import com.avito.instrumentation.internal.executing.ExecutionParameters
import com.avito.instrumentation.internal.suite.filter.ImpactAnalysisResult
import com.avito.logger.LoggerFactory
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.createStubInstance
import com.avito.utils.gradle.KubernetesCredentials
import java.io.File

internal fun InstrumentationTestsAction.Params.Companion.createStubInstance(
    mainApk: File = File(""),
    testApk: File = File(""),
    instrumentationConfiguration: InstrumentationConfiguration.Data =
        InstrumentationConfiguration.Data.createStubInstance(),
    executionParameters: ExecutionParameters = ExecutionParameters.createStubInstance(),
    buildId: String = "33456",
    buildType: String = "teamcity",
    kubernetesCredentials: KubernetesCredentials = KubernetesCredentials.Service(
        token = "empty",
        caCertData = "empty",
        url = "empty"
    ),
    projectName: String = "testProject",
    suppressFailure: Boolean = false,
    suppressFlaky: Boolean = false,
    impactAnalysisResult: ImpactAnalysisResult = ImpactAnalysisResult.createStubInstance(),
    loggerFactory: LoggerFactory,
    outputDir: File = File("."),
    verdictFile: File = File(outputDir, "verdict.json"),
    reportViewerUrl: String = "https://reports",
    fileStorageUrl: String = "https://files",
    reportCoordinates: ReportCoordinates = ReportCoordinates.createStubInstance(),
    statsDConfig: StatsDConfig = StatsDConfig.Disabled,
    useInMemoryReport: Boolean = false,
    uploadTestArtifacts: Boolean = false
) = InstrumentationTestsAction.Params(
    mainApk = mainApk,
    testApk = testApk,
    instrumentationConfiguration = instrumentationConfiguration,
    executionParameters = executionParameters,
    buildId = buildId,
    buildType = buildType,
    kubernetesCredentials = kubernetesCredentials,
    projectName = projectName,
    suppressFailure = suppressFailure,
    suppressFlaky = suppressFlaky,
    impactAnalysisResult = impactAnalysisResult,
    loggerFactory = loggerFactory,
    outputDir = outputDir,
    verdictFile = verdictFile,
    reportViewerUrl = reportViewerUrl,
    fileStorageUrl = fileStorageUrl,
    statsDConfig = statsDConfig,
    legacyReportFactory = object : LegacyReportFactory {

        override fun createReport(config: LegacyReportFactory.Config): Report = StubReport()

        override fun createLegacyReport(config: LegacyReportFactory.Config): LegacyReport = StubReport()

        override fun createReadReport(config: LegacyReportFactory.Config): ReadReport = StubReport()
    },
    legacyReportConfig = LegacyReportFactory.Config.ReportViewerCoordinates(
        ReportCoordinates.createStubInstance(),
        buildId
    ),
    reportCoordinates = reportCoordinates,
    proguardMappings = emptyList(),
    useInMemoryReport = useInMemoryReport,
    uploadTestArtifacts = uploadTestArtifacts
)
