package com.avito.runner.scheduler.runner

import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.service.model.DeviceTestCaseRun

data class TestRunnerResult(
    val runs: Map<TestRunRequest, List<DeviceTestCaseRun>>
)
