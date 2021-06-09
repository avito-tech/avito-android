package com.avito.runner.service.model.intention

import com.avito.runner.service.model.TestCase

data class InstrumentationTestRunAction(
    val test: TestCase,
    val testPackage: String,
    val targetPackage: String,
    val testRunner: String,
    val instrumentationParams: Map<String, String>,
    val executionNumber: Int,
    val timeoutMinutes: Long,
    val enableDeviceDebug: Boolean
) {

    override fun toString(): String = "Run ${test.testName} test"

    companion object
}
