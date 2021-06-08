package com.avito.runner.scheduler.runner.model

import com.avito.runner.service.model.TestCase
import com.avito.runner.service.model.createStubInstance
import com.avito.runner.service.worker.device.model.DeviceConfiguration
import com.avito.runner.service.worker.device.model.createStubInstance

internal fun generateTestRunRequest(
    testCase: TestCase = TestCase.createStubInstance(),
    deviceConfiguration: DeviceConfiguration = DeviceConfiguration.createStubInstance(),
    application: String = "",
    applicationPackage: String = "",
    testApplication: String = "",
    testPackage: String = "",
    testRunner: String = "",
    timeoutMinutes: Long = 0,
    enableDeviceDebug: Boolean = true,
    scheduling: TestRunRequest.Scheduling = TestRunRequest.Scheduling(
        retryCount = 0,
        minimumSuccessCount = 1,
        minimumFailedCount = 0
    )
): TestRunRequest = TestRunRequest(
    testCase = testCase,
    configuration = deviceConfiguration,
    application = application,
    testApplication = testApplication,
    applicationPackage = applicationPackage,
    testPackage = testPackage,
    testRunner = testRunner,
    timeoutMinutes = timeoutMinutes,
    instrumentationParameters = emptyMap(),
    scheduling = scheduling,
    enableDeviceDebug = enableDeviceDebug
)
