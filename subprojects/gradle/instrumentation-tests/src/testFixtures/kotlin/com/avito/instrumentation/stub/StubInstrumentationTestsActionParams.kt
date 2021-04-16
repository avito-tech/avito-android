package com.avito.instrumentation.stub

import com.avito.android.runner.report.AvitoReport
import com.avito.android.runner.report.ReadReport
import com.avito.android.runner.report.Report
import com.avito.android.runner.report.ReportFactory
import com.avito.android.runner.report.StubReport
import com.avito.android.stats.StatsDConfig
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.createStubInstance
import com.avito.instrumentation.internal.InstrumentationTestsAction
import com.avito.instrumentation.internal.executing.ExecutionParameters
import com.avito.instrumentation.internal.suite.filter.ImpactAnalysisResult
import com.avito.logger.LoggerFactory
import com.avito.report.ReportLinkGenerator
import com.avito.report.TestSuiteNameProvider
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
    fileStorageUrl: String = "https://files",
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
    fileStorageUrl = fileStorageUrl,
    statsDConfig = statsDConfig,
    reportFactory = object : ReportFactory {

        override fun createReport(): Report {
            return StubReport()
        }

        override fun createReadReport(): ReadReport {
            return StubReport()
        }

        override fun createAvitoReport(): AvitoReport {
            return StubReport()
        }

        override fun createReportLinkGenerator(): ReportLinkGenerator {
            return ReportLinkGenerator.Stub
        }

        override fun createTestSuiteNameGenerator(): TestSuiteNameProvider {
            return TestSuiteNameProvider.Stub
        }
    },
    proguardMappings = emptyList(),
    useInMemoryReport = useInMemoryReport,
    uploadTestArtifacts = uploadTestArtifacts,
    reportViewerConfig = null
)
