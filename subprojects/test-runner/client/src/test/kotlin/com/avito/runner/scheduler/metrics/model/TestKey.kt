package com.avito.runner.scheduler.metrics.model

import com.avito.test.model.DeviceName
import com.avito.test.model.TestCase
import com.avito.test.model.TestName

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
