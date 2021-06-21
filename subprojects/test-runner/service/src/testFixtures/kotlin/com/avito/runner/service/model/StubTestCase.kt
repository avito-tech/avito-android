package com.avito.runner.service.model

import com.avito.report.model.DeviceName
import com.avito.report.model.TestName

fun TestCase.Companion.createStubInstance(
    className: String = "com.avito.Test",
    methodName: String = "test",
    deviceName: DeviceName = DeviceName("api29")
): TestCase = TestCase(
    name = TestName(className, methodName),
    deviceName = deviceName
)
