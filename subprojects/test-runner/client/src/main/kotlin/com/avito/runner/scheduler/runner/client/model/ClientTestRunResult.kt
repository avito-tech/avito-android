package com.avito.runner.scheduler.runner.client.model

import com.avito.runner.scheduler.runner.scheduler.TestExecutionState
import com.avito.runner.service.model.DeviceTestCaseRun

data class ClientTestRunResult(
    val state: TestExecutionState,
    val incomingTestCaseRun: DeviceTestCaseRun
)
