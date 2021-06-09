package com.avito.instrumentation.service

import com.avito.instrumentation.internal.InstrumentationTestsActionFactory
import com.avito.runner.config.InstrumentationTestsActionParams
import com.avito.runner.scheduler.runner.model.TestSchedulerResult
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

@Suppress("UnstableApiUsage")
public abstract class TestRunnerService : BuildService<TestRunnerService.Params>, AutoCloseable {

    internal interface Params : BuildServiceParameters

    internal fun runTests(
        params: TestRunParams,
        legacyParams: InstrumentationTestsActionParams
    ): TestSchedulerResult {

        val metricsConfig = RunnerMetricsConfig(
            statsDConfig = legacyParams.statsDConfig,
            runnerPrefix = params.metricsPrefix
        )

        val factory = createTestsActionFactory(legacyParams, metricsConfig)

        return factory.provideScheduler().schedule()
    }

    override fun close() {
    }

    private fun createTestsActionFactory(
        params: InstrumentationTestsActionParams,
        metricsConfig: RunnerMetricsConfig
    ): InstrumentationTestsActionFactory {
        return InstrumentationTestsActionFactory.Impl(params, metricsConfig)
    }
}
