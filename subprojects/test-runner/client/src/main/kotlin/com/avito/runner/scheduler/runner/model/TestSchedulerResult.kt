package com.avito.runner.scheduler.runner.model

import com.avito.report.model.AndroidTest
import com.avito.runner.scheduler.suite.TestSuite

public data class TestSchedulerResult(
    val testSuite: TestSuite,
    val testResults: Collection<AndroidTest>
)
