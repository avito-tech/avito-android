package com.avito.instrumentation

import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.executing.ExecutionParameters
import com.avito.instrumentation.report.FakeReport
import com.avito.instrumentation.report.ReadReport
import com.avito.instrumentation.report.Report
import com.avito.instrumentation.suite.filter.ImpactAnalysisResult
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.createStubInstance
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.logging.CILogger
import java.io.File

fun InstrumentationTestsAction.Params.Companion.createStubInstance(
    mainApk: File = File(""),
    testApk: File = File(""),
    apkOnTargetCommit: File = File(""),
    testApkOnTargetCommit: File = File(""),
    instrumentationConfiguration: InstrumentationConfiguration.Data = InstrumentationConfiguration.Data.createStubInstance(),
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
    logger: CILogger = CILogger.allToStdout,
    sendStatistics: Boolean = false,
    slackToken: String = "slack",
    sourceCommitHash: String = "",
    currentBranch: String = "develop",
    outputDir: File = File("."),
    reportViewerUrl: String = "https://reports",
    kubernetesRegistry: String = "",
    fileStorageUrl: String = "https://files",
    reportCoordinates: ReportCoordinates = ReportCoordinates.createStubInstance()
) =
    InstrumentationTestsAction.Params(
        mainApk = mainApk,
        testApk = testApk,
        apkOnTargetCommit = apkOnTargetCommit,
        testApkOnTargetCommit = testApkOnTargetCommit,
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
        logger = logger,
        currentBranch = currentBranch,
        sourceCommitHash = sourceCommitHash,
        outputDir = outputDir,
        sendStatistics = sendStatistics,
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
                return FakeReport()
            }

            override fun createReadReport(config: Report.Factory.Config): ReadReport {
                return FakeReport()
            }
        },
        reportCoordinates = reportCoordinates,
        proguardMappings = emptyList()
    )
