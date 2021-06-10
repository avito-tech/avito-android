package com.avito.runner.scheduler.runner.model

import com.avito.runner.service.model.DeviceTestCaseRun

internal data class TestRunnerResult(
    val runs: Map<TestRunRequest, List<DeviceTestCaseRun>>
)
