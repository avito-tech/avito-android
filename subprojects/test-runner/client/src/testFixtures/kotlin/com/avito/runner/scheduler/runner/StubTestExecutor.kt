package com.avito.runner.scheduler.runner

import com.avito.android.runner.devices.internal.createKubernetesDeviceProvider
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDConfig
import com.avito.logger.LoggerFactory
import com.avito.runner.scheduler.listener.TestLifecycleListener
import com.avito.runner.scheduler.runner.model.TestWithTarget
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.time.StubTimeProvider
import com.avito.time.TimeProvider
import java.io.File

public class StubTestExecutor : TestExecutor {

    override fun execute(
        application: File?,
        testApplication: File,
        testsToRun: List<TestWithTarget>,
        executionParameters: ExecutionParameters,
        output: File
    ) {
        // empty
    }
}

public fun TestExecutor.Companion.createStubInstance(
    loggerFactory: LoggerFactory,
    configurationName: String = "",
    adb: Adb = Adb(),
    timeProvider: TimeProvider = StubTimeProvider(),
    metricsConfig: RunnerMetricsConfig = RunnerMetricsConfig(
        statsDConfig = StatsDConfig.Disabled,
        runnerPrefix = SeriesName.create("test")
    )
): TestExecutor = TestExecutorImpl(
    devicesProvider = createKubernetesDeviceProvider(
        adb = adb,
        loggerFactory = loggerFactory,
        timeProvider = timeProvider
    ),
    testReporter = TestLifecycleListener.STUB,
    configurationName = configurationName,
    loggerFactory = loggerFactory,
    metricsConfig = metricsConfig,
    saveTestArtifactsToOutputs = false,
    fetchLogcatForIncompleteTests = false,
)
