package com.avito.runner.scheduler.util

import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.worker.device.model.DeviceConfiguration
import com.avito.runner.test.generateDeviceConfiguration
import com.avito.runner.test.generateTestCase
import com.avito.runner.test.randomLong
import com.avito.runner.test.randomString

fun generateTestRunRequest(
    testCase: TestCase = generateTestCase(),
    deviceConfiguration: DeviceConfiguration = generateDeviceConfiguration(),
    application: String = randomString(),
    applicationPackage: String = randomString(),
    testApplication: String = randomString(),
    testPackage: String = randomString(),
    testRunner: String = randomString(),
    timeoutMinutes: Long = randomLong(),
    scheduling: TestRunRequest.Scheduling = TestRunRequest.Scheduling(
        retryCount = 0,
        minimumSuccessCount = 1,
        minimumFailedCount = 0
    )
): TestRunRequest =
    TestRunRequest(
        testCase = testCase,
        configuration = deviceConfiguration,
        application = application,
        testApplication = testApplication,
        applicationPackage = applicationPackage,
        testPackage = testPackage,
        testRunner = testRunner,
        timeoutMinutes = timeoutMinutes,
        instrumentationParameters = emptyMap(),
        scheduling = scheduling
    )
