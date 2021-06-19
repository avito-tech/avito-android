package com.avito.runner.scheduler.runner.model

import com.avito.runner.service.model.TestCase
import com.avito.runner.service.worker.device.model.DeviceConfiguration
import java.io.File

internal class TestRunRequestFactory(
    private val application: File?,
    private val testApplication: File,
    private val executionParameters: ExecutionParameters
) {

    fun create(test: TestWithTarget): TestRunRequest {
        val reservation = test.target.reservation
        val quota = test.target.reservation.quota
        return TestRunRequest(
            testCase = TestCase(
                name = test.test.name,
                deviceName = test.target.deviceName
            ),
            configuration = DeviceConfiguration(
                api = reservation.device.api,
                model = reservation.device.model
            ),
            scheduling = TestRunRequest.Scheduling(
                retryCount = quota.retryCount,
                minimumFailedCount = quota.minimumFailedCount,
                minimumSuccessCount = quota.minimumSuccessCount
            ),
            application = application?.absolutePath,
            applicationPackage = executionParameters.applicationPackageName,
            testApplication = testApplication.absolutePath,
            testPackage = executionParameters.applicationTestPackageName,
            testRunner = executionParameters.testRunner,
            timeoutMinutes = TEST_TIMEOUT_MINUTES,
            instrumentationParameters = test.target.instrumentationParams,
            enableDeviceDebug = executionParameters.enableDeviceDebug
        )
    }
}

private const val TEST_TIMEOUT_MINUTES = 5L
