package com.avito.runner.service.model.intention

import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.TestCase

sealed class Action {

    data class InstrumentationTestRunAction(
        val test: TestCase,
        val testPackage: String,
        val targetPackage: String,
        val testRunner: String,
        val instrumentationParams: Map<String, String>,
        val executionNumber: Int,
        val timeoutMinutes: Long
    ) : Action() {
        override fun toString(): String = "Run ${test.testName} test"
    }
}

sealed class ActionResult {

    data class InstrumentationTestRunActionResult(
        val testCaseRun: DeviceTestCaseRun
    ) : ActionResult()
}
