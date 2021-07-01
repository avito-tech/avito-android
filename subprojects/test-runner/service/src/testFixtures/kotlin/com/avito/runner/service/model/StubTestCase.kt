package com.avito.runner.service.model

import com.avito.test.model.DeviceName
import com.avito.test.model.TestCase
import com.avito.test.model.TestName

fun TestCase.Companion.createStubInstance(
    className: String = "com.avito.Test",
    methodName: String = "test",
    deviceName: DeviceName = DeviceName("api29")
): TestCase = TestCase(
    name = TestName(className, methodName),
    deviceName = deviceName
)
