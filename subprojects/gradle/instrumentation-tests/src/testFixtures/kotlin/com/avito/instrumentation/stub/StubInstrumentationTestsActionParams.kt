package com.avito.instrumentation.stub

import com.avito.android.runner.report.ReadReport
import com.avito.android.runner.report.Report
import com.avito.android.runner.report.StubReport
import com.avito.android.runner.report.factory.ReportFactory
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
    reportFactory = object : ReportFactory {

        override fun createReport(config: ReportFactory.Config): Report = StubReport()

        override fun createReadReport(config: ReportFactory.Config): ReadReport = StubReport()
    },
    reportConfig = ReportFactory.Config.ReportViewerCoordinates(
        ReportCoordinates.createStubInstance(),
        buildId
    ),
    reportCoordinates = reportCoordinates,
    proguardMappings = emptyList(),
    uploadTestArtifacts = uploadTestArtifacts
)
