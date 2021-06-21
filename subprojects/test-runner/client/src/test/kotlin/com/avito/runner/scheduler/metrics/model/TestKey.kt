package com.avito.runner.scheduler.metrics.model

import com.avito.report.model.DeviceName
import com.avito.report.model.TestName
import com.avito.runner.service.model.TestCase

// todo move to :service
public fun TestCase.Companion.createStubInstance(
    className: String = "com.avito.Test",
    methodName: String = "test",
    deviceName: DeviceName = DeviceName("api22")
): TestCase = TestCase(TestName(className, methodName), deviceName)

internal fun TestKey.Companion.createStubInstance(
    testCase: TestCase = TestCase.createStubInstance(),
    executionNumber: Int = 0
) = TestKey(testCase, executionNumber)

internal fun String.toTestKey() = TestKey.createStubInstance(
    TestCase.createStubInstance(methodName = this)
)
