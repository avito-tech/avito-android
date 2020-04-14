package com.avito.instrumentation.scheduling

import com.avito.instrumentation.rerun.BuildOnTargetCommitForTest
import java.util.ArrayDeque
import java.util.Queue

internal class FakeTestsScheduler(
    results: List<TestsScheduler.Result> = emptyList()
) : TestsScheduler {

    private val scheduleResultsQueue: Queue<TestsScheduler.Result> = ArrayDeque(results)

    override fun schedule(
        buildOnTargetCommitResult: BuildOnTargetCommitForTest.Result
    ): TestsScheduler.Result {
        if (scheduleResultsQueue.isEmpty()) {
            throw IllegalArgumentException(
                "Schedule results queue is empty in FakeTestsScheduler"
            )
        }

        return scheduleResultsQueue.poll()
    }
}
