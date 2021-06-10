package com.avito.runner.scheduler.suite

import com.avito.runner.scheduler.runner.model.TestWithTarget
import com.avito.runner.scheduler.suite.filter.TestsFilter

public data class TestSuite(
    val appliedFilter: TestsFilter,
    val testsToRun: List<TestWithTarget>,
    val skippedTests: List<Pair<TestWithTarget, TestsFilter.Result.Excluded>>
)
