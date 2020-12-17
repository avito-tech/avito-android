package com.avito.instrumentation

import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.executing.ExecutionParameters
import com.avito.instrumentation.finalizer.InstrumentationTestActionFinalizer
import com.avito.instrumentation.report.Report
import com.avito.instrumentation.scheduling.TestsScheduler
import com.avito.instrumentation.suite.filter.ImpactAnalysisResult
import com.avito.report.model.ReportCoordinates
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
    constructor(params: Params) : this(params, InstrumentationTestsActionFactory.Impl(params))

    constructor(params: Params, factory: InstrumentationTestsActionFactory) : this(
        params = params,
        logger = params.logger,
        scheduler = factory.provideScheduler(),
        finalizer = factory.provideFinalizer()
    )

    override fun run() {
        logger.debug("Starting instrumentation tests action for configuration: ${params.instrumentationConfiguration}")
        logger.debug(
            "Impact analysis: ${params.impactAnalysisResult.policy.javaClass.simpleName} " +
                "${params.impactAnalysisResult}"
        )

        val testsExecutionResults = scheduler.schedule()

        finalizer.finalize(
            testsExecutionResults = testsExecutionResults
        )
    }

    data class Params(
        val mainApk: File?,
        val testApk: File,
        val instrumentationConfiguration: InstrumentationConfiguration.Data,
        val executionParameters: ExecutionParameters,
        val buildId: String,
        val buildType: String,
        val buildUrl: String,
        val currentBranch: String,
        val sourceCommitHash: String,
        val kubernetesCredentials: KubernetesCredentials,
        val projectName: String,
        val suppressFailure: Boolean,
        val suppressFlaky: Boolean,
        val impactAnalysisResult: ImpactAnalysisResult,
        val logger: CILogger,
        val outputDir: File,
        val verdictFile: File,
        val slackToken: String,
        val reportViewerUrl: String,
        val fileStorageUrl: String,
        val registry: String,
        val reportFactory: Report.Factory,
        val reportConfig: Report.Factory.Config,
        val reportCoordinates: ReportCoordinates,
        val proguardMappings: List<File>
    ) : Serializable {
        companion object
    }
}
