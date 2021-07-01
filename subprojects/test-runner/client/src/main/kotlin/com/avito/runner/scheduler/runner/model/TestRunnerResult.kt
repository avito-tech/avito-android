package com.avito.runner.scheduler.runner.model

import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.test.model.TestCase

internal data class TestRunnerResult(
    val runs: Map<TestCase, List<DeviceTestCaseRun>>
)
