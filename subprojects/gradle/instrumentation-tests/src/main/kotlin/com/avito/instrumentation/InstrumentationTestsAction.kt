package com.avito.instrumentation

import com.avito.android.stats.StatsDConfig
import com.avito.bitbucket.BitbucketConfig
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.executing.ExecutionParameters
import com.avito.instrumentation.finalizer.InstrumentationTestActionFinalizer
import com.avito.instrumentation.report.Report
import com.avito.instrumentation.scheduling.TestsScheduler
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.Team
import com.avito.slack.model.SlackChannel
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.logging.CILogger
import java.io.File
import java.io.Serializable
import javax.inject.Inject

class InstrumentationTestsAction(
    private val params: Params,
    private val logger: CILogger,
    private val scheduler: TestsScheduler,
    private val finalizer: InstrumentationTestActionFinalizer
) : Runnable {

    /**
     * for Worker API
     */
    @Suppress("unused")
    @Inject
    constructor(params: Params) : this(params, InstrumentatioTestsActionFactory.Impl(params))

    constructor(params: Params, factory: InstrumentatioTestsActionFactory) : this(
        params = params,
        logger = params.logger,
        scheduler = factory.provideScheduler(),
        finalizer = factory.provideFinalizer()
    )

    override fun run() {
        logger.debug("Starting instrumentation tests action for configuration: ${params.instrumentationConfiguration}")

        val testsExecutionResults = scheduler.schedule()

        finalizer.finalize(
            testsExecutionResults = testsExecutionResults
        )
    }

    data class Params(
        val mainApk: File?,
        val testApk: File,
        val apkOnTargetCommit: File?,
        val testApkOnTargetCommit: File?,
        val instrumentationConfiguration: InstrumentationConfiguration.Data,
        val executionParameters: ExecutionParameters,
        val buildId: String,
        val buildType: String,
        val pullRequestId: Int?,
        val buildUrl: String,
        val currentBranch: String,
        val sourceCommitHash: String,
        val kubernetesCredentials: KubernetesCredentials,
        val projectName: String,
        val suppressFailure: Boolean,
        val suppressFlaky: Boolean,
        val impactAnalysisResult: File?,
        val logger: CILogger,
        val outputDir: File,
        val sendStatistics: Boolean,
        val isFullTestSuite: Boolean,
        val slackToken: String,
        val reportViewerUrl: String,
        val fileStorageUrl: String,
        val bitbucketConfig: BitbucketConfig,
        val statsdConfig: StatsDConfig,
        val unitToChannelMapping: Map<Team, SlackChannel>,
        val registry: String,
        val reportFactory: Report.Factory,
        val reportConfig: Report.Factory.Config,
        @Deprecated("Will be removed")
        val reportCoordinates: ReportCoordinates,
        val proguardMappings: List<File>
    ) : Serializable {
        companion object
    }
}
