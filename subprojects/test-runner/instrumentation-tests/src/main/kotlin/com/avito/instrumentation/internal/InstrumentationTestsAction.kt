package com.avito.instrumentation.internal

import com.avito.android.runner.devices.DeviceProviderFactoryImpl
import com.avito.android.runner.devices.DevicesProviderFactory
import com.avito.android.stats.SeriesName
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.config.InstrumentationTestsActionParams
import com.avito.runner.finalizer.Finalizer
import com.avito.runner.scheduler.runner.scheduler.TestScheduler
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import com.avito.utils.BuildFailer
import javax.inject.Inject

internal class InstrumentationTestsAction(
    private val params: InstrumentationTestsActionParams,
    loggerFactory: LoggerFactory,
    private val scheduler: TestScheduler,
    private val finalizer: Finalizer,
    private val buildFailer: BuildFailer,
) : Runnable {

    private val logger = loggerFactory.create<InstrumentationTestsAction>()

    /**
     * for Worker API
     */
    @Suppress("unused")
    @Inject
    constructor(params: InstrumentationTestsActionParams) : this(
        params = params,
        factory = InstrumentationTestsActionFactory.Impl(
            params,
            RunnerMetricsConfig(params.statsDConfig, runnerPrefix(params))
        )
    )

    constructor(params: InstrumentationTestsActionParams, factory: InstrumentationTestsActionFactory) : this(
        params = params,
        buildFailer = BuildFailer.RealFailer(),
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

        when (val result = finalizer.finalize(testSchedulerResults = testSchedulerResults)) {
            Finalizer.Result.Ok -> {
                // no op
            }
            is Finalizer.Result.Failure -> buildFailer.failBuild(result.message)
        }
    }
}

private fun runnerPrefix(params: InstrumentationTestsActionParams) = SeriesName.create(
    "testrunner",
    params.projectName,
    params.instrumentationConfiguration.name
)

private fun createDevicesProviderFactory(
    params: InstrumentationTestsActionParams,
    timeProvider: TimeProvider
): DevicesProviderFactory = DeviceProviderFactoryImpl(
    kubernetesCredentials = params.kubernetesCredentials,
    buildId = params.buildId,
    buildType = params.buildType,
    loggerFactory = params.loggerFactory,
    timeProvider = timeProvider,
    statsDConfig = params.statsDConfig
)
