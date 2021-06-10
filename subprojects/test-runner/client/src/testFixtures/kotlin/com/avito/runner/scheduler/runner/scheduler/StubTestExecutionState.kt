package com.avito.runner.scheduler.runner.scheduler

import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.service.model.DeviceTestCaseRun

internal class StubTestExecutionState(
    override val request: TestRunRequest
) : TestExecutionState {

    override fun verdict(incomingTestCaseRun: DeviceTestCaseRun?): TestExecutionState.Verdict =
        TestExecutionState.Verdict.SendResult(emptyList())
}
