package com.avito.runner.scheduler.report.model

import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.TestCaseRun

class TestCaseRequestMatchingReport(
    val request: TestRunRequest,
    val runs: List<DeviceTestCaseRun>,
    val result: Result
) {

    val successRuns: Int by lazy {
        runs.count { it.testCaseRun.result is TestCaseRun.Result.Passed }
    }

    val failedRuns: Int by lazy {
        runs.count { it.testCaseRun.result is TestCaseRun.Result.Failed }
    }

    val totalRuns: Int by lazy {
        runs.size
    }

    val durationMilliseconds: Long by lazy {
        runs.map { it.testCaseRun.durationMilliseconds }.sum()
    }

    sealed class Result {
        object Matched : Result()
        object Mismatched : Result()
        object Ignored : Result()
    }
}
