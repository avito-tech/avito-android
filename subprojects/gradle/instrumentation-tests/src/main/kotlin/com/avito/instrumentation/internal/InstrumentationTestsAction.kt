package com.avito.instrumentation.internal

import com.avito.android.runner.devices.DeviceProviderFactoryImpl
import com.avito.android.runner.devices.DevicesProviderFactory
import com.avito.android.runner.report.ReportViewerConfig
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDConfig
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.internal.executing.ExecutionParameters
import com.avito.instrumentation.internal.finalizer.InstrumentationTestActionFinalizer
import com.avito.instrumentation.internal.scheduling.TestsScheduler
import com.avito.instrumentation.internal.suite.filter.ImpactAnalysisResult
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import com.avito.utils.gradle.KubernetesCredentials
import java.io.File
import java.io.Serializable
import javax.inject.Inject

internal class InstrumentationTestsAction(
    private val params: Params,
    private val loggerFactory: LoggerFactory,
    private val scheduler: TestsScheduler,
    private val finalizer: InstrumentationTestActionFinalizer
) : Runnable {

    private val logger = loggerFactory.create<InstrumentationTestsAction>()

    /**
     * for Worker API
     */
    @Suppress("unused")
    @Inject
    constructor(params: Params) : this(
        params = params,
        factory = InstrumentationTestsActionFactory.Impl(
            params,
            RunnerMetricsConfig(params.statsDConfig, runnerPrefix(params))
        )
    )

    constructor(params: Params, factory: InstrumentationTestsActionFactory) : this(
        params = params,
        loggerFactory = params.loggerFactory,
        scheduler = factory.provideScheduler(
            devicesProviderFactory = createDevicesProviderFactory(
                params = params,
                timeProvider = DefaultTimeProvider()
            )
        ),
        finalizer = factory.provideFinalizer()
    )

    override fun run() {
        logger.debug("Starting instrumentation tests action for configuration: ${params.instrumentationConfiguration}")
        logger.debug("Impact analysis: ${params.impactAnalysisResult}")

        val testSchedulerResults = scheduler.schedule()

        finalizer.finalize(testSchedulerResults = testSchedulerResults)
    }

    data class Params(
        val mainApk: File?,
        val testApk: File,
        val instrumentationConfiguration: InstrumentationConfiguration.Data,
        val executionParameters: ExecutionParameters,
        val buildId: String,
        val buildType: String,
        val kubernetesCredentials: KubernetesCredentials,
        val projectName: String,
        val suppressFailure: Boolean,
        val suppressFlaky: Boolean,
        val impactAnalysisResult: ImpactAnalysisResult,
        val loggerFactory: LoggerFactory,
        val outputDir: File,
        val verdictFile: File,
        val fileStorageUrl: String,
        val statsDConfig: StatsDConfig,
        val reportViewerConfig: ReportViewerConfig?,
        val proguardMappings: List<File>,
        val useInMemoryReport: Boolean,
        val uploadTestArtifacts: Boolean
    ) : Serializable {
        companion object
    }
}

private fun runnerPrefix(params: InstrumentationTestsAction.Params) = SeriesName.create(
    "testrunner",
    params.projectName,
    params.instrumentationConfiguration.name
)

private fun createDevicesProviderFactory(
    params: InstrumentationTestsAction.Params,
    timeProvider: TimeProvider
): DevicesProviderFactory = DeviceProviderFactoryImpl(
    kubernetesCredentials = params.kubernetesCredentials,
    buildId = params.buildId,
    buildType = params.buildType,
    loggerFactory = params.loggerFactory,
    timeProvider = timeProvider,
    statsDConfig = params.statsDConfig
)
