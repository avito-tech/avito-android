package com.avito.instrumentation.service

import com.avito.android.stats.StatsDConfig
import com.avito.instrumentation.internal.InstrumentationTestsActionFactory
import com.avito.runner.config.InstrumentationTestsActionParams
import com.avito.runner.finalizer.Finalizer
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.utils.BuildFailer
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

@Suppress("UnstableApiUsage")
public abstract class TestRunnerWorkAction : WorkAction<TestRunnerWorkAction.Params> {

    internal interface Params : WorkParameters {

        val service: Property<TestRunnerService>

        val statsDConfig: Property<StatsDConfig>

        val testRunParams: Property<TestRunParams>

        // todo replace with testRunParams completely ; knows too much
        val legacyTestRunParams: Property<InstrumentationTestsActionParams>
    }

    override fun execute() {

        val params = parameters.testRunParams.get()

        val metricsConfig = RunnerMetricsConfig(
            statsDConfig = parameters.statsDConfig.get(),
            runnerPrefix = params.metricsPrefix
        )

        val legacyTestRunParams = parameters.legacyTestRunParams.get()

        val factory = createTestsActionFactory(legacyTestRunParams, metricsConfig)

        val testResults = parameters.service.get().runTests(params, legacyTestRunParams)

        val buildFailer: BuildFailer = BuildFailer.RealFailer()

        when (val result = factory.provideFinalizer().finalize(testResults)) {
            Finalizer.Result.Ok -> {
            }
            is Finalizer.Result.Failure -> buildFailer.failBuild(result.message)
        }
    }

    private fun createTestsActionFactory(
        params: InstrumentationTestsActionParams,
        metricsConfig: RunnerMetricsConfig
    ): InstrumentationTestsActionFactory {
        return InstrumentationTestsActionFactory.Impl(params, metricsConfig)
    }
}
