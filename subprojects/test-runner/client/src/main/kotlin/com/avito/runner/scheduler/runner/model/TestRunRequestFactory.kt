package com.avito.runner.scheduler.runner.model

import com.avito.runner.config.TargetConfigurationData
import com.avito.runner.service.worker.device.model.DeviceConfiguration
import com.avito.test.model.DeviceName
import com.avito.test.model.TestCase
import java.io.File

internal class TestRunRequestFactory(
    private val application: File?,
    private val testApplication: File,
    private val executionParameters: ExecutionParameters,
    private val targets: Map<DeviceName, TargetConfigurationData>
) {

    fun create(test: TestCase): TestRunRequest {
        val target = requireNotNull(targets[test.deviceName]) {
            "Can't find target ${test.deviceName}"
        }
        val reservation = target.reservation
        val quota = target.reservation.quota
        return TestRunRequest(
            testCase = TestCase(
                name = test.name,
                deviceName = test.deviceName
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
            instrumentationParameters = target.instrumentationParams,
            enableDeviceDebug = executionParameters.enableDeviceDebug
        )
    }
}

private const val TEST_TIMEOUT_MINUTES = 5L
