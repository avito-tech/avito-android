package com.avito.runner.service.model.intention

import com.avito.runner.service.model.TestCase
import com.avito.runner.service.model.createStubInstance

fun InstrumentationTestRunAction.Companion.createStubInstance(
    testCase: TestCase = TestCase.createStubInstance(),
    testPackage: String = "",
    targetPackage: String = "",
    testRunner: String = "",
    instrumentationParams: Map<String, String> = emptyMap(),
    executionNumber: Int = 0,
    timeoutMinutes: Long = 0,
    enableDeviceDebug: Boolean = false
): InstrumentationTestRunAction = InstrumentationTestRunAction(
    test = testCase,
    testPackage = testPackage,
    targetPackage = targetPackage,
    testRunner = testRunner,
    instrumentationParams = instrumentationParams,
    executionNumber = executionNumber,
    timeoutMinutes = timeoutMinutes,
    enableDeviceDebug = enableDeviceDebug
)
