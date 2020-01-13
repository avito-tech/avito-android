package com.avito.runner.scheduler.runner.scheduler.retry

import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.TestCaseRun
import kotlin.math.max
import kotlin.math.min

class SchedulingBasedRetryManager(
    private val scheduling: TestRunRequest.Scheduling
) : RetryManager {

    override fun retryCount(history: List<DeviceTestCaseRun>): Int {
        val runQuota = scheduling.retryCount + 1
        val remainingTries = runQuota - history.size

        val passedCount = history
            .count { it.testCaseRun.result is TestCaseRun.Result.Passed }
        val failedCount = history
            .count { it.testCaseRun.result is TestCaseRun.Result.Failed }

        val passedResultsRequired = max(scheduling.minimumSuccessCount - passedCount, 0)
        val failedResultsRequired = max(scheduling.minimumFailedCount - failedCount, 0)

        val result = calculateMinimumTestsToSchedule(
            passedRequired = passedResultsRequired,
            failedRequired = failedResultsRequired,
            remainingTries = remainingTries
        )

        return max(result, 0)
    }

    private fun calculateMinimumTestsToSchedule(
        passedRequired: Int,
        failedRequired: Int,
        remainingTries: Int
    ): Int {
        val runsRequired = passedRequired + failedRequired
        val redundantQuota = remainingTries - runsRequired

        return min(
            min(passedRequired, failedRequired) + redundantQuota + 1,
            runsRequired
        )
    }
}
