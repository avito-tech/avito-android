package com.avito.runner.scheduler.runner.model

import com.avito.runner.service.model.DeviceTestCaseRun

data class TestRunResult(
    val request: TestRunRequest,
    val result: List<DeviceTestCaseRun>
)
