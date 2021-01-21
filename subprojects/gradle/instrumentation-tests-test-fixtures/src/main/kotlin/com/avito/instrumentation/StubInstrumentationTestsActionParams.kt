package com.avito.instrumentation

import com.avito.android.stats.StatsDConfig
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.executing.ExecutionParameters
import com.avito.instrumentation.report.ReadReport
import com.avito.instrumentation.report.Report
import com.avito.instrumentation.report.StubReport
import com.avito.instrumentation.suite.filter.ImpactAnalysisResult
import com.avito.logger.LoggerFactory
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.createStubInstance
import com.avito.utils.gradle.KubernetesCredentials
import java.io.File

fun InstrumentationTestsAction.Params.Companion.createStubInstance(
    mainApk: File = File(""),
    testApk: File = File(""),
    instrumentationConfiguration: InstrumentationConfiguration.Data =
        InstrumentationConfiguration.Data.createStubInstance(),
    executionParameters: ExecutionParameters = ExecutionParameters.createStubInstance(),
    buildId: String = "33456",
    buildType: String = "teamcity",
    buildUrl: String = "https://build",
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
    slackToken: String = "slack",
    sourceCommitHash: String = "",
    currentBranch: String = "develop",
    outputDir: File = File("."),
    verdictFile: File = File(outputDir, "verdict.json"),
    reportViewerUrl: String = "https://reports",
    kubernetesRegistry: String = "",
    fileStorageUrl: String = "https://files",
    reportCoordinates: ReportCoordinates = ReportCoordinates.createStubInstance(),
    statsDConfig: StatsDConfig = StatsDConfig.Disabled
) =
    InstrumentationTestsAction.Params(
        mainApk = mainApk,
        testApk = testApk,
        instrumentationConfiguration = instrumentationConfiguration,
        executionParameters = executionParameters,
        buildId = buildId,
        buildType = buildType,
        buildUrl = buildUrl,
        kubernetesCredentials = kubernetesCredentials,
        projectName = projectName,
        suppressFailure = suppressFailure,
        suppressFlaky = suppressFlaky,
        impactAnalysisResult = impactAnalysisResult,
        loggerFactory = loggerFactory,
        currentBranch = currentBranch,
        sourceCommitHash = sourceCommitHash,
        outputDir = outputDir,
        verdictFile = verdictFile,
        slackToken = slackToken,
        fileStorageUrl = fileStorageUrl,
        reportViewerUrl = reportViewerUrl,
        registry = kubernetesRegistry,
        reportConfig = Report.Factory.Config.ReportViewerCoordinates(
            ReportCoordinates.createStubInstance(),
            buildId
        ),
        reportFactory = object : Report.Factory {
            override fun createReport(config: Report.Factory.Config): Report {
                return StubReport()
            }

            override fun createReadReport(config: Report.Factory.Config): ReadReport {
                return StubReport()
            }
        },
        reportCoordinates = reportCoordinates,
        proguardMappings = emptyList(),
        statsDConfig = statsDConfig
    )
