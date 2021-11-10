package com.avito.runner.config

import com.avito.android.stats.StatsDConfig
import com.avito.runner.scheduler.report.ReportViewerConfig
import com.avito.runner.scheduler.runner.model.ExecutionParameters
import com.avito.runner.scheduler.suite.filter.ImpactAnalysisResult
import com.avito.utils.gradle.KubernetesCredentials
import java.io.File
import java.io.Serializable

public data class RunnerInputParams(
    val mainApk: File?,
    val testApk: File,
    val instrumentationConfiguration: InstrumentationConfigurationData,
    val executionParameters: ExecutionParameters,
    val buildId: String,
    val buildType: String,
    val kubernetesCredentials: KubernetesCredentials,
    val deviceDebug: Boolean,
    val projectName: String,
    val suppressFailure: Boolean,
    val suppressFlaky: Boolean,
    val impactAnalysisResult: ImpactAnalysisResult,
    val outputDir: File,
    val verdictFile: File,
    val fileStorageUrl: String,
    val statsDConfig: StatsDConfig,
    val reportViewerConfig: ReportViewerConfig?,
    val proguardMappings: List<File>,
    val uploadTestArtifacts: Boolean,
    val fetchLogcatForIncompleteTests: Boolean,
    val saveTestArtifactsToOutputs: Boolean,
    val useLegacyExtensionsV1Beta: Boolean,
    val sendPodsMetrics: Boolean,
) : Serializable {

    public companion object
}
