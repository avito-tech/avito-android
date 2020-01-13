package com.avito.runner.scheduler.util.mock

import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.scheduler.runner.scheduler.TestExecutionState
import com.avito.runner.service.model.DeviceTestCaseRun

class MockTestExecutionState(
    override val request: TestRunRequest
) : TestExecutionState {

    override fun verdict(incomingTestCaseRun: DeviceTestCaseRun?): TestExecutionState.Verdict =
        TestExecutionState.Verdict.SendResult(emptyList())
}
