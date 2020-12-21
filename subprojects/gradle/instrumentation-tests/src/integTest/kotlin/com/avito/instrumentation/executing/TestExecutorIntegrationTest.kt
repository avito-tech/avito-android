package com.avito.instrumentation.executing

import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.instrumentation.configuration.target.scheduling.quota.QuotaConfiguration
import com.avito.instrumentation.createStubInstance
import com.avito.instrumentation.report.listener.StubTestReporter
import com.avito.instrumentation.reservation.client.kubernetes.KubernetesReservationClient
import com.avito.instrumentation.reservation.client.kubernetes.createStubInstance
import com.avito.instrumentation.reservation.devices.provider.KubernetesDevicesProvider
import com.avito.instrumentation.reservation.request.Device
import com.avito.instrumentation.reservation.request.Reservation
import com.avito.instrumentation.reservation.request.createStubInstance
import com.avito.instrumentation.suite.model.TestWithTarget
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.createStubInstance
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.runner.service.worker.device.adb.AdbDevicesManager
import com.avito.utils.logging.CILogger
import com.avito.utils.logging.StubCILogger
import com.avito.utils.logging.commonLogger
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class TestExecutorIntegrationTest {

    private val logger = StubCILogger()

    @Test
    fun `executor - does not stuck - when no requests passed`(@TempDir tempDir: File) {
        val executor = createTestExecutor(logger = logger)

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
        val executor = createTestExecutor(logger = logger)

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
                            quota = QuotaConfiguration.Data(
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
        logger: CILogger,
        configurationName: String = "",
        adb: Adb = Adb()
    ): TestExecutor = TestExecutor.Impl(
        devicesProvider = KubernetesDevicesProvider(
            client = KubernetesReservationClient.createStubInstance(
                logger = logger,
                adb = adb
            ),
            adbDevicesManager = AdbDevicesManager(
                logger = commonLogger(logger),
                adb = adb
            ),
            logger = logger,
            adb = adb
        ),
        testReporter = StubTestReporter(),
        configurationName = configurationName,
        logger = logger
    )
}
