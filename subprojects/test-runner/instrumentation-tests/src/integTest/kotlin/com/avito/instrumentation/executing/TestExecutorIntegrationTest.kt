package com.avito.instrumentation.executing

import com.avito.android.runner.devices.model.createStubInstance
import com.avito.instrumentation.reservation.request.Device
import com.avito.logger.StubLoggerFactory
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.createStubInstance
import com.avito.runner.config.QuotaConfigurationData
import com.avito.runner.config.Reservation
import com.avito.runner.config.TargetConfigurationData
import com.avito.runner.config.createStubInstance
import com.avito.runner.scheduler.runner.ExecutionParameters
import com.avito.runner.scheduler.runner.TestExecutor
import com.avito.runner.scheduler.runner.createStubInstance
import com.avito.runner.scheduler.runner.model.TestWithTarget
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class TestExecutorIntegrationTest {

    private val loggerFactory = StubLoggerFactory

    @Test
    fun `executor - does not stuck - when no requests passed`(@TempDir tempDir: File) {
        val executor = TestExecutor.createStubInstance(loggerFactory = loggerFactory)

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
        val executor = TestExecutor.createStubInstance(loggerFactory = loggerFactory)

        executor.execute(
            application = null,
            testApplication = File(""),
            testsToRun = listOf(
                TestWithTarget(
                    test = TestStaticDataPackage.createStubInstance(),
                    target = TargetConfigurationData.createStubInstance(
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
}
