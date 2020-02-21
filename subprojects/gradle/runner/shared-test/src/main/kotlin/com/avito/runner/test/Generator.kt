package com.avito.runner.test

import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.model.intention.InstrumentationTestRunAction
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.State
import com.avito.runner.service.worker.device.model.DeviceConfiguration
import com.avito.runner.service.worker.device.model.DeviceData

fun generateInstalledApplicationLayer(
    applicationPath: String = randomString(),
    applicationPackage: String = randomString()
): State.Layer.InstalledApplication =
    State.Layer.InstalledApplication(
        applicationPath = applicationPath,
        applicationPackage = applicationPackage
    )

fun generateIntention(
    state: State,
    action: InstrumentationTestRunAction
): Intention =
    Intention(
        state = state,
        action = action
    )

fun generateInstrumentationTestAction(
    test: TestCase = generateTestCase(),
    testPackage: String = randomString(),
    targetPackage: String = randomString(),
    testRunner: String = randomString(),
    instrumentationParams: Map<String, String> = emptyMap(),
    timeoutMinutes: Long = randomLong(),
    executionNumber: Int = randomInt()
): InstrumentationTestRunAction =
    InstrumentationTestRunAction(
        test = test,
        testRunner = testRunner,
        targetPackage = targetPackage,
        testPackage = testPackage,
        instrumentationParams = instrumentationParams,
        timeoutMinutes = timeoutMinutes,
        executionNumber = executionNumber
    )


fun generateDeviceTestCaseRun(
    testCaseRun: TestCaseRun = generateTestCaseRun(),
    deviceData: DeviceData = generateDeviceData()
): DeviceTestCaseRun =
    DeviceTestCaseRun(
        testCaseRun = testCaseRun,
        device = deviceData
    )

fun generateDeviceData(
    serial: String = randomString(),
    configuration: DeviceConfiguration = generateDeviceConfiguration()
): DeviceData = DeviceData(
    serial = serial,
    configuration = configuration
)

fun generateDeviceConfiguration(
    api: Int = randomInt(),
    model: String = randomString()
): DeviceConfiguration = DeviceConfiguration(
    api = api,
    model = model
)

fun generateTestCase(
    className: String = randomString(),
    methodName: String = randomString(),
    deviceName: String = randomString()
): TestCase =
    TestCase(
        className = className,
        methodName = methodName,
        deviceName = deviceName
    )

fun generateTestCaseRun(
    testCase: TestCase = generateTestCase(),
    result: TestCaseRun.Result = TestCaseRun.Result.Passed,
    timestampStartedMilliseconds: Long = 1000,
    timestampCompletedMilliseconds: Long = 2000
): TestCaseRun =
    TestCaseRun(
        test = testCase,
        result = result,
        timestampStartedMilliseconds = timestampStartedMilliseconds,
        timestampCompletedMilliseconds = timestampCompletedMilliseconds
    )
