package com.avito.runner.service.model

fun TestCase.Companion.createStubInstance(
    className: String = "com.avito.Test",
    methodName: String = "test",
    deviceName: String = "api29"
): TestCase = TestCase(
    className = className,
    methodName = methodName,
    deviceName = deviceName
)
