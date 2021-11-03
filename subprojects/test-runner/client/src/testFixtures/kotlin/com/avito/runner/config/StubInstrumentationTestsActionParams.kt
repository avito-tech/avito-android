package com.avito.runner.config

import com.avito.android.stats.StatsDConfig
import com.avito.runner.scheduler.runner.createStubInstance
import com.avito.runner.scheduler.runner.model.ExecutionParameters
import com.avito.runner.scheduler.suite.filter.ImpactAnalysisResult
import com.avito.runner.scheduler.suite.filter.createStubInstance
import com.avito.utils.gradle.KubernetesCredentials
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory

@OptIn(ExperimentalPathApi::class)
public fun RunnerInputParams.Companion.createStubInstance(
    mainApk: File = File(""),
    testApk: File = File(""),
    instrumentationConfiguration: InstrumentationConfigurationData =
        InstrumentationConfigurationData.createStubInstance(),
    executionParameters: ExecutionParameters = ExecutionParameters.createStubInstance(),
    buildId: String = "33456",
    buildType: String = "teamcity",
    kubernetesCredentials: KubernetesCredentials = KubernetesCredentials.Service(
        token = "empty",
        caCertData = "empty",
        url = "empty",
        namespace = "kubernetesNamespace",
    ),
    projectName: String = "testProject",
    suppressFailure: Boolean = false,
    suppressFlaky: Boolean = false,
    impactAnalysisResult: ImpactAnalysisResult = ImpactAnalysisResult.createStubInstance(),
    deviceDebug: Boolean = false,
    outputDir: File = createTempDirectory("runnerOutput").toFile(),
    verdictFile: File = File(outputDir, "verdict.json"),
    fileStorageUrl: String = "https://files",
    statsDConfig: StatsDConfig = StatsDConfig.Disabled,
    uploadTestArtifacts: Boolean = false,
    fetchLogcatForIncompleteTests: Boolean = false,
    saveTestArtifactsToOutputs: Boolean = false,
    useLegacyExtensionsV1Beta: Boolean = true,
    sendPodsMetrics: Boolean = true,
): RunnerInputParams = RunnerInputParams(
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
    outputDir = outputDir,
    verdictFile = verdictFile,
    fileStorageUrl = fileStorageUrl,
    statsDConfig = statsDConfig,
    proguardMappings = emptyList(),
    uploadTestArtifacts = uploadTestArtifacts,
    reportViewerConfig = null,
    saveTestArtifactsToOutputs = saveTestArtifactsToOutputs,
    fetchLogcatForIncompleteTests = fetchLogcatForIncompleteTests,
    useLegacyExtensionsV1Beta = useLegacyExtensionsV1Beta,
    sendPodsMetrics = sendPodsMetrics,
    deviceDebug = deviceDebug
)
