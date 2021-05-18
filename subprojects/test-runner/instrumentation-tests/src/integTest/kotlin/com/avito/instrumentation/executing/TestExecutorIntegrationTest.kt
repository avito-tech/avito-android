package com.avito.instrumentation.executing

import com.avito.android.runner.devices.internal.createKubernetesDeviceProvider
import com.avito.android.runner.devices.model.createStubInstance
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDConfig
import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.instrumentation.createStubInstance
import com.avito.instrumentation.internal.executing.ExecutionParameters
import com.avito.instrumentation.internal.executing.TestExecutor
import com.avito.instrumentation.internal.executing.TestExecutorImpl
import com.avito.instrumentation.internal.suite.model.TestWithTarget
import com.avito.instrumentation.reservation.request.Device
import com.avito.instrumentation.reservation.request.QuotaConfigurationData
import com.avito.instrumentation.reservation.request.Reservation
import com.avito.logger.LoggerFactory
import com.avito.logger.StubLoggerFactory
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.createStubInstance
import com.avito.runner.scheduler.listener.TestLifecycleListener
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.time.StubTimeProvider
import com.avito.time.TimeProvider
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class TestExecutorIntegrationTest {

    private val loggerFactory = StubLoggerFactory

    @Test
    fun `executor - does not stuck - when no requests passed`(@TempDir tempDir: File) {
        val executor = createTestExecutor(loggerFactory = loggerFactory)

        executor.execute(
            application = null,
            testApplication = File(""),
            testsToRun = listOf(),
            executionParameters = ExecutionParameters.createStubInstance(),
            output = tempDir
        )

        // no assertion needed: just stuck in runBlocking forever before fix
    }

    @Test
    fun `executor - does not stuck - when deployment failed with invalid image reference`(@TempDir tempDir: File) {
        val executor = createTestExecutor(loggerFactory = loggerFactory)

        executor.execute(
            application = null,
            testApplication = File(""),
            testsToRun = listOf(
                TestWithTarget(
                    test = TestStaticDataPackage.createStubInstance(),
                    target = TargetConfiguration.Data.createStubInstance(
                        reservation = Reservation.StaticReservation(
                            device = Device.CloudEmulator.createStubInstance(image = "invalid/image/reference"),
                            count = 1,
                            quota = QuotaConfigurationData(
                                retryCount = 0,
                                minimumSuccessCount = 1,
                                minimumFailedCount = 0
                            )
                        )
                    )
                )
            ),
            executionParameters = ExecutionParameters.createStubInstance(),
            output = tempDir
        )

        // no assertion needed: just stuck in runBlocking forever before fix
    }

    private fun createTestExecutor(
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
        metricsConfig = metricsConfig
    )
}
