package com.avito.runner.scheduler.runner.model

import com.avito.runner.service.model.createStubInstance
import com.avito.runner.service.worker.device.model.DeviceConfiguration
import com.avito.runner.service.worker.device.model.createStubInstance
import com.avito.test.model.TestCase

internal fun TestRunRequest.Companion.createStubInstance(
    testCase: TestCase = TestCase.createStubInstance(),
    deviceConfiguration: DeviceConfiguration = DeviceConfiguration.createStubInstance(),
    application: String = "/application/path",
    applicationPackage: String = "com.avito.app",
    testApplication: String = "/test-application/path",
    testPackage: String = "com.avito.app.test",
    testRunner: String = "com.avito.StubInstrumentationTestRunner",
    timeoutMinutes: Long = 5,
    instrumentationParameters: Map<String, String> = emptyMap(),
    enableDeviceDebug: Boolean = false,
    scheduling: TestRunRequest.Scheduling = TestRunRequest.Scheduling.createStubInstance()
): TestRunRequest = TestRunRequest(
    testCase = testCase,
    configuration = deviceConfiguration,
    application = application,
    testApplication = testApplication,
    applicationPackage = applicationPackage,
    testPackage = testPackage,
    testRunner = testRunner,
    timeoutMinutes = timeoutMinutes,
    instrumentationParameters = instrumentationParameters,
    scheduling = scheduling,
    enableDeviceDebug = enableDeviceDebug
)

internal fun TestRunRequest.Scheduling.Companion.createStubInstance(
    retryCount: Int = 0,
    minimumSuccessCount: Int = 1,
    minimumFailedCount: Int = 0
): TestRunRequest.Scheduling = TestRunRequest.Scheduling(
    retryCount = retryCount,
    minimumSuccessCount = minimumSuccessCount,
    minimumFailedCount = minimumFailedCount
)
