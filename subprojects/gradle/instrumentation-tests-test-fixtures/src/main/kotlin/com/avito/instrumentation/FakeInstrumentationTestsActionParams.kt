package com.avito.instrumentation

import com.avito.android.stats.StatsDConfig
import com.avito.bitbucket.AtlassianCredentials
import com.avito.bitbucket.BitbucketConfig
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.executing.ExecutionParameters
import com.avito.report.model.Team
import com.avito.slack.model.SlackChannel
import com.avito.test.gradle.randomCommitHash
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
    targetCommit: String = randomCommitHash(),
    kubernetesCredentials: KubernetesCredentials = KubernetesCredentials.Service(
        token = "empty",
        caCertData = "empty",
        url = "empty"
    ),
    projectName: String = "testProject",
    suppressFailure: Boolean = false,
    suppressFlaky: Boolean = false,
    impactAnalysisResult: File? = null,
    logger: CILogger = CILogger.allToStdout,
    sendStatistics: Boolean = false,
    slackToken: String = "slack",
    sourceCommitHash: String = "",
    currentBranch: String = "develop",
    outputDir: File = File("."),
    reportId: String? = null,
    reportApiUrl: String = "https://reports",
    reportApiFallbackUrl: String = "https://reports",
    reportViewerUrl: String = "https://reports",
    kubernetesRegistry: String = "",
    fileStorageUrl: String = "https://files",
    pullRequestId: Int? = null,
    isFullTestSuite: Boolean = false,
    bitbucketConfig: BitbucketConfig = BitbucketConfig(
        baseUrl = "http://bitbucket",
        credentials = AtlassianCredentials("xxx", "xxx"),
        projectKey = "AA",
        repositorySlug = "android"
    ),
    statsDConfig: StatsDConfig = StatsDConfig(false, "", "", 0, ""),
    unitToChannelMapping: Map<Team, SlackChannel> = emptyMap()
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
        targetCommit = targetCommit,
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
        reportId = reportId,
        reportApiUrl = reportApiUrl,
        fileStorageUrl = fileStorageUrl,
        pullRequestId = pullRequestId,
        isFullTestSuite = isFullTestSuite,
        bitbucketConfig = bitbucketConfig,
        statsdConfig = statsDConfig,
        unitToChannelMapping = unitToChannelMapping,
        reportViewerUrl = reportViewerUrl,
        reportApiFallbackUrl = reportApiFallbackUrl,
        registry = kubernetesRegistry
    )
