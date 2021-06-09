package com.avito.runner.scheduler.runner.scheduler

import com.avito.report.model.AndroidTest
import com.avito.runner.scheduler.suite.TestSuite

public interface TestsScheduler {

    public fun schedule(): Result

    public data class Result(
        val testSuite: TestSuite,
        val testResults: Collection<AndroidTest>
    )
}
